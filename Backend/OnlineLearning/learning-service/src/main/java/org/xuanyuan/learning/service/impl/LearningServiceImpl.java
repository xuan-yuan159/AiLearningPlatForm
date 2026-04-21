package org.xuanyuan.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xuanyuan.common.exception.BaseException;
import org.xuanyuan.learning.dto.CourseCardDto;
import org.xuanyuan.learning.dto.CourseProgressResp;
import org.xuanyuan.learning.dto.EnrollmentCourseDto;
import org.xuanyuan.learning.dto.LearningHomeResp;
import org.xuanyuan.learning.dto.ProgressSummaryDto;
import org.xuanyuan.learning.dto.StudyProgressReportReq;
import org.xuanyuan.learning.entity.Chapter;
import org.xuanyuan.learning.entity.Course;
import org.xuanyuan.learning.entity.CourseEnrollment;
import org.xuanyuan.learning.entity.Resource;
import org.xuanyuan.learning.entity.StudyRecord;
import org.xuanyuan.learning.mapper.ChapterMapper;
import org.xuanyuan.learning.mapper.CourseEnrollmentMapper;
import org.xuanyuan.learning.mapper.CourseMapper;
import org.xuanyuan.learning.mapper.ResourceMapper;
import org.xuanyuan.learning.mapper.StudyRecordMapper;
import org.xuanyuan.learning.service.LearningService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningServiceImpl implements LearningService {

    private static final int COURSE_STATUS_PUBLISHED = 1;
    private static final int COURSE_STATUS_DELETED = 3;
    private static final int ENROLLMENT_STATUS_LEARNING = 1;
    private static final int ENROLLMENT_STATUS_COMPLETED = 2;
    private static final int RECORD_COMPLETED = 1;

    private final CourseEnrollmentMapper courseEnrollmentMapper;
    private final StudyRecordMapper studyRecordMapper;
    private final CourseMapper courseMapper;
    private final ChapterMapper chapterMapper;
    private final ResourceMapper resourceMapper;

    /**
     * 学生选课（幂等）
     */
    @Override
    public Long enrollCourse(Long studentId, Long courseId) {
        Course course = getPublishedCourse(courseId);
        CourseEnrollment existed = courseEnrollmentMapper.selectOne(new LambdaQueryWrapper<CourseEnrollment>()
                .eq(CourseEnrollment::getUserId, studentId)
                .eq(CourseEnrollment::getCourseId, course.getId()));
        if (existed != null) {
            return existed.getId();
        }

        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setUserId(studentId);
        enrollment.setCourseId(courseId);
        enrollment.setStatus(ENROLLMENT_STATUS_LEARNING);
        LocalDateTime now = LocalDateTime.now();
        enrollment.setEnrolledAt(now);
        enrollment.setUpdatedAt(now);
        courseEnrollmentMapper.insert(enrollment);
        return enrollment.getId();
    }

    /**
     * 分页查询我的课程
     */
    @Override
    public Page<EnrollmentCourseDto> listMyEnrollments(Long studentId, Integer status, Integer page, Integer size) {
        int current = normalizePage(page);
        int pageSize = normalizeSize(size);

        LambdaQueryWrapper<CourseEnrollment> wrapper = new LambdaQueryWrapper<CourseEnrollment>()
                .eq(CourseEnrollment::getUserId, studentId)
                .orderByDesc(CourseEnrollment::getUpdatedAt);
        if (status != null) {
            wrapper.eq(CourseEnrollment::getStatus, status);
        }
        Page<CourseEnrollment> enrollmentPage = courseEnrollmentMapper.selectPage(new Page<>(current, pageSize), wrapper);
        List<CourseEnrollment> enrollments = enrollmentPage.getRecords();
        if (enrollments == null || enrollments.isEmpty()) {
            return new Page<>(current, pageSize, enrollmentPage.getTotal());
        }

        List<Long> courseIds = enrollments.stream().map(CourseEnrollment::getCourseId).filter(Objects::nonNull).distinct().toList();
        Map<Long, Course> courseMap = selectCoursesByIds(courseIds, false).stream()
                .collect(Collectors.toMap(Course::getId, Function.identity(), (left, right) -> left));
        Map<Long, CourseProgressSnapshot> progressMap = buildCourseProgressSnapshots(studentId, courseIds);

        List<EnrollmentCourseDto> resultList = new ArrayList<>();
        for (CourseEnrollment enrollment : enrollments) {
            Course course = courseMap.get(enrollment.getCourseId());
            if (course == null || Objects.equals(course.getStatus(), COURSE_STATUS_DELETED)) {
                continue;
            }
            CourseProgressSnapshot snapshot = progressMap.getOrDefault(course.getId(), CourseProgressSnapshot.empty());
            EnrollmentCourseDto dto = new EnrollmentCourseDto();
            dto.setEnrollmentId(enrollment.getId());
            dto.setCourseId(course.getId());
            dto.setTitle(course.getTitle());
            dto.setCoverUrl(course.getCoverUrl());
            dto.setCategory(course.getCategory());
            dto.setDifficulty(course.getDifficulty());
            dto.setStatus(enrollment.getStatus());
            dto.setProgressPercent(snapshot.progressPercent());
            dto.setTotalResources(snapshot.totalResources());
            dto.setCompletedResources(snapshot.completedResources());
            dto.setTotalWatchedS(snapshot.totalWatchedS());
            dto.setLastStudiedAt(snapshot.lastStudiedAt());
            dto.setEnrolledAt(enrollment.getEnrolledAt());
            resultList.add(dto);
        }

        Page<EnrollmentCourseDto> result = new Page<>(current, pageSize, enrollmentPage.getTotal());
        result.setRecords(resultList);
        return result;
    }

    /**
     * 上报学习进度
     */
    @Override
    public void reportProgress(Long studentId, StudyProgressReportReq req) {
        Course course = getPublishedCourse(req.getCourseId());
        Resource resource = getResourceAndValidateCourse(req.getResourceId(), course.getId());

        Long chapterId = req.getChapterId() != null ? req.getChapterId() : resource.getChapterId();
        if (chapterId != null) {
            Chapter chapter = chapterMapper.selectById(chapterId);
            if (chapter == null || !Objects.equals(chapter.getCourseId(), course.getId())) {
                throw new BaseException(400, "Chapter does not belong to current course");
            }
        }

        ensureEnrollment(studentId, course.getId()); // 未选课时自动补选课关系

        StudyRecord record = studyRecordMapper.selectOne(new LambdaQueryWrapper<StudyRecord>()
                .eq(StudyRecord::getUserId, studentId)
                .eq(StudyRecord::getResourceId, resource.getId()));

        if (record == null) {
            StudyRecord newRecord = new StudyRecord();
            newRecord.setUserId(studentId);
            newRecord.setCourseId(course.getId());
            newRecord.setChapterId(chapterId);
            newRecord.setResourceId(resource.getId());
            newRecord.setProgressSec(safeNonNegative(req.getProgressSec()));
            newRecord.setTotalWatchedS(safeNonNegative(req.getTotalWatchedS()));
            newRecord.setCompleted(Boolean.TRUE.equals(req.getCompleted()) ? RECORD_COMPLETED : 0);
            LocalDateTime now = LocalDateTime.now();
            newRecord.setLastStudiedAt(now);
            newRecord.setCreatedAt(now);
            newRecord.setUpdatedAt(now);
            studyRecordMapper.insert(newRecord);
        } else {
            record.setCourseId(course.getId());
            record.setChapterId(chapterId);
            record.setProgressSec(safeNonNegative(req.getProgressSec())); // 覆盖最新进度
            int newTotal = safeNonNegative(record.getTotalWatchedS()) + safeNonNegative(req.getTotalWatchedS());
            record.setTotalWatchedS(newTotal); // 累计学习时长
            record.setCompleted(Boolean.TRUE.equals(req.getCompleted()) || Objects.equals(record.getCompleted(), RECORD_COMPLETED) ? RECORD_COMPLETED : 0);
            LocalDateTime now = LocalDateTime.now();
            record.setLastStudiedAt(now);
            record.setUpdatedAt(now);
            studyRecordMapper.updateById(record);
        }

        refreshEnrollmentStatus(studentId, course.getId());
    }

    /**
     * 查询课程进度详情
     */
    @Override
    public CourseProgressResp getCourseProgress(Long studentId, Long courseId) {
        Course course = getPublishedCourse(courseId);
        CourseEnrollment enrollment = courseEnrollmentMapper.selectOne(new LambdaQueryWrapper<CourseEnrollment>()
                .eq(CourseEnrollment::getUserId, studentId)
                .eq(CourseEnrollment::getCourseId, courseId));
        if (enrollment == null) {
            throw new BaseException(400, "Please enroll this course first");
        }

        List<Chapter> chapters = chapterMapper.selectList(new LambdaQueryWrapper<Chapter>()
                .eq(Chapter::getCourseId, courseId)
                .orderByAsc(Chapter::getSortOrder)
                .orderByAsc(Chapter::getId));
        List<Resource> resources = resourceMapper.selectList(new LambdaQueryWrapper<Resource>()
                .eq(Resource::getCourseId, courseId)
                .orderByAsc(Resource::getChapterId)
                .orderByAsc(Resource::getSortOrder)
                .orderByAsc(Resource::getId));
        List<StudyRecord> records = studyRecordMapper.selectList(new LambdaQueryWrapper<StudyRecord>()
                .eq(StudyRecord::getUserId, studentId)
                .eq(StudyRecord::getCourseId, courseId));
        Map<Long, StudyRecord> recordMap = records.stream()
                .filter(item -> item.getResourceId() != null)
                .collect(Collectors.toMap(StudyRecord::getResourceId, Function.identity(), (left, right) -> left));

        Map<Long, CourseProgressResp.ChapterProgressDto> chapterMap = new LinkedHashMap<>();
        for (Chapter chapter : chapters) {
            CourseProgressResp.ChapterProgressDto chapterDto = new CourseProgressResp.ChapterProgressDto();
            chapterDto.setChapterId(chapter.getId());
            chapterDto.setTitle(chapter.getTitle());
            chapterDto.setSortOrder(chapter.getSortOrder());
            chapterMap.put(chapter.getId(), chapterDto);
        }

        if (resources.stream().anyMatch(item -> item.getChapterId() == null)) {
            CourseProgressResp.ChapterProgressDto unassigned = new CourseProgressResp.ChapterProgressDto();
            unassigned.setChapterId(0L);
            unassigned.setTitle("Unassigned");
            unassigned.setSortOrder(Integer.MAX_VALUE);
            chapterMap.put(0L, unassigned);
        }

        int completedResources = 0;
        int totalWatchedS = 0;
        LocalDateTime lastStudiedAt = null;
        for (Resource resource : resources) {
            CourseProgressResp.ResourceProgressDto resourceDto = new CourseProgressResp.ResourceProgressDto();
            resourceDto.setResourceId(resource.getId());
            resourceDto.setTitle(resource.getTitle());
            resourceDto.setType(resource.getType());
            resourceDto.setUrl(resource.getUrl());
            resourceDto.setDurationS(resource.getDurationS());
            resourceDto.setSortOrder(resource.getSortOrder());

            StudyRecord record = recordMap.get(resource.getId());
            if (record != null) {
                resourceDto.setProgressSec(safeNonNegative(record.getProgressSec()));
                resourceDto.setTotalWatchedS(safeNonNegative(record.getTotalWatchedS()));
                resourceDto.setCompleted(safeNonNegative(record.getCompleted()));
                resourceDto.setLastStudiedAt(record.getLastStudiedAt());
                totalWatchedS += safeNonNegative(record.getTotalWatchedS());
                if (Objects.equals(record.getCompleted(), RECORD_COMPLETED)) {
                    completedResources++;
                }
                if (record.getLastStudiedAt() != null && (lastStudiedAt == null || record.getLastStudiedAt().isAfter(lastStudiedAt))) {
                    lastStudiedAt = record.getLastStudiedAt();
                }
            } else {
                resourceDto.setProgressSec(0);
                resourceDto.setTotalWatchedS(0);
                resourceDto.setCompleted(0);
            }

            Long chapterId = resource.getChapterId() == null ? 0L : resource.getChapterId();
            CourseProgressResp.ChapterProgressDto chapterDto = chapterMap.get(chapterId);
            if (chapterDto == null) {
                chapterDto = new CourseProgressResp.ChapterProgressDto();
                chapterDto.setChapterId(chapterId);
                chapterDto.setTitle("Unassigned");
                chapterDto.setSortOrder(Integer.MAX_VALUE);
                chapterMap.put(chapterId, chapterDto);
            }
            chapterDto.getResources().add(resourceDto);
        }

        CourseProgressResp resp = new CourseProgressResp();
        resp.setCourseId(course.getId());
        resp.setCourseTitle(course.getTitle());
        resp.setTotalResources(resources.size());
        resp.setCompletedResources(completedResources);
        resp.setProgressPercent(computeProgressPercent(completedResources, resources.size()));
        resp.setTotalWatchedS(totalWatchedS);
        resp.setLastStudiedAt(lastStudiedAt);
        resp.setChapters(new ArrayList<>(chapterMap.values()));
        return resp;
    }

    /**
     * 查询学生首页聚合数据
     */
    @Override
    public LearningHomeResp getLearningHome(Long studentId) {
        LearningHomeResp resp = new LearningHomeResp();
        resp.setSummary(buildSummary(studentId));
        resp.setRecentLearningCourses(buildRecentLearningCourses(studentId, 5));
        resp.setLearningCourses(buildLearningCourses(studentId, 6));
        resp.setRecommendedCourses(recommendCourses(studentId, 6));
        return resp;
    }

    /**
     * 查询推荐课程
     */
    @Override
    public List<CourseCardDto> recommendCourses(Long studentId, Integer limit) {
        int normalizedLimit = normalizeRecommendLimit(limit);
        List<CourseEnrollment> enrollments = courseEnrollmentMapper.selectList(new LambdaQueryWrapper<CourseEnrollment>()
                .eq(CourseEnrollment::getUserId, studentId));
        Set<Long> enrolledCourseIds = enrollments.stream()
                .map(CourseEnrollment::getCourseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Course> enrolledCourses = enrolledCourseIds.isEmpty()
                ? Collections.emptyList()
                : selectCoursesByIds(enrolledCourseIds, false);
        Set<String> preferredCategories = enrolledCourses.stream()
                .map(Course::getCategory)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        Set<String> preferredTags = enrolledCourses.stream()
                .flatMap(item -> splitTags(item.getTags()).stream())
                .collect(Collectors.toSet());

        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<Course>()
                .eq(Course::getStatus, COURSE_STATUS_PUBLISHED)
                .orderByDesc(Course::getPublishedAt)
                .orderByDesc(Course::getCreatedAt);
        if (!enrolledCourseIds.isEmpty()) {
            queryWrapper.notIn(Course::getId, enrolledCourseIds);
        }
        List<Course> candidates = courseMapper.selectList(queryWrapper);
        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Integer> hotMap = loadHotMap();
        List<ScoredCourse> scored = new ArrayList<>();
        for (Course candidate : candidates) {
            int hot = hotMap.getOrDefault(candidate.getId(), 0);
            int overlap = countOverlap(splitTags(candidate.getTags()), preferredTags);
            double score = hot * 0.1D;
            if (preferredCategories.contains(candidate.getCategory())) {
                score += 3D;
            }
            score += Math.min(overlap, 3);
            scored.add(new ScoredCourse(candidate, hot, score));
        }

        Comparator<ScoredCourse> comparator;
        if (preferredCategories.isEmpty() && preferredTags.isEmpty()) {
            comparator = Comparator.comparingInt(ScoredCourse::hotCount).reversed()
                    .thenComparing(item -> item.course().getPublishedAt(), Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(item -> item.course().getCreatedAt(), Comparator.nullsLast(Comparator.reverseOrder()));
        } else {
            comparator = Comparator.comparingDouble(ScoredCourse::score).reversed()
                    .thenComparingInt(ScoredCourse::hotCount).reversed()
                    .thenComparing(item -> item.course().getPublishedAt(), Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(item -> item.course().getCreatedAt(), Comparator.nullsLast(Comparator.reverseOrder()));
        }

        return scored.stream()
                .sorted(comparator)
                .limit(normalizedLimit)
                .map(item -> toCourseCard(item.course(), ENROLLMENT_STATUS_LEARNING, CourseProgressSnapshot.empty()))
                .toList();
    }

    /**
     * 生成首页汇总信息
     */
    private ProgressSummaryDto buildSummary(Long studentId) {
        List<CourseEnrollment> enrollments = courseEnrollmentMapper.selectList(new LambdaQueryWrapper<CourseEnrollment>()
                .eq(CourseEnrollment::getUserId, studentId));
        int learningCount = (int) enrollments.stream().filter(item -> Objects.equals(item.getStatus(), ENROLLMENT_STATUS_LEARNING)).count();
        int completedCount = (int) enrollments.stream().filter(item -> Objects.equals(item.getStatus(), ENROLLMENT_STATUS_COMPLETED)).count();

        List<StudyRecord> records = studyRecordMapper.selectList(new LambdaQueryWrapper<StudyRecord>()
                .eq(StudyRecord::getUserId, studentId));
        int totalWatched = records.stream().mapToInt(item -> safeNonNegative(item.getTotalWatchedS())).sum();

        ProgressSummaryDto summary = new ProgressSummaryDto();
        summary.setLearningCourseCount(learningCount);
        summary.setCompletedCourseCount(completedCount);
        summary.setTotalWatchedS(totalWatched);
        return summary;
    }

    /**
     * 生成最近学习课程列表
     */
    private List<CourseCardDto> buildRecentLearningCourses(Long studentId, int limit) {
        List<StudyRecord> records = studyRecordMapper.selectList(new LambdaQueryWrapper<StudyRecord>()
                .eq(StudyRecord::getUserId, studentId)
                .orderByDesc(StudyRecord::getLastStudiedAt));
        if (records.isEmpty()) {
            return Collections.emptyList();
        }

        LinkedHashMap<Long, StudyRecord> latestByCourse = new LinkedHashMap<>();
        for (StudyRecord record : records) {
            if (record.getCourseId() == null || latestByCourse.containsKey(record.getCourseId())) {
                continue;
            }
            latestByCourse.put(record.getCourseId(), record);
            if (latestByCourse.size() >= limit) {
                break;
            }
        }

        List<Long> courseIds = new ArrayList<>(latestByCourse.keySet());
        Map<Long, Course> courseMap = selectCoursesByIds(courseIds, false).stream()
                .collect(Collectors.toMap(Course::getId, Function.identity(), (left, right) -> left));
        Map<Long, Integer> statusMap = loadEnrollmentStatusMap(studentId, courseIds);
        Map<Long, CourseProgressSnapshot> snapshotMap = buildCourseProgressSnapshots(studentId, courseIds);

        List<CourseCardDto> result = new ArrayList<>();
        for (Long courseId : courseIds) {
            Course course = courseMap.get(courseId);
            if (course == null || Objects.equals(course.getStatus(), COURSE_STATUS_DELETED)) {
                continue;
            }
            int status = statusMap.getOrDefault(courseId, ENROLLMENT_STATUS_LEARNING);
            CourseProgressSnapshot snapshot = snapshotMap.getOrDefault(courseId, CourseProgressSnapshot.empty());
            result.add(toCourseCard(course, status, snapshot));
        }
        return result;
    }

    /**
     * 生成学习中课程列表
     */
    private List<CourseCardDto> buildLearningCourses(Long studentId, int limit) {
        List<CourseEnrollment> enrollments = courseEnrollmentMapper.selectList(new LambdaQueryWrapper<CourseEnrollment>()
                .eq(CourseEnrollment::getUserId, studentId)
                .eq(CourseEnrollment::getStatus, ENROLLMENT_STATUS_LEARNING)
                .orderByDesc(CourseEnrollment::getUpdatedAt));
        if (enrollments.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> courseIds = enrollments.stream()
                .map(CourseEnrollment::getCourseId)
                .filter(Objects::nonNull)
                .distinct()
                .limit(limit)
                .toList();
        Map<Long, Course> courseMap = selectCoursesByIds(courseIds, false).stream()
                .collect(Collectors.toMap(Course::getId, Function.identity(), (left, right) -> left));
        Map<Long, CourseProgressSnapshot> snapshotMap = buildCourseProgressSnapshots(studentId, courseIds);

        List<CourseCardDto> result = new ArrayList<>();
        for (Long courseId : courseIds) {
            Course course = courseMap.get(courseId);
            if (course == null || Objects.equals(course.getStatus(), COURSE_STATUS_DELETED)) {
                continue;
            }
            result.add(toCourseCard(course, ENROLLMENT_STATUS_LEARNING, snapshotMap.getOrDefault(courseId, CourseProgressSnapshot.empty())));
        }
        return result;
    }

    /**
     * 加载热度映射
     */
    private Map<Long, Integer> loadHotMap() {
        List<Map<String, Object>> rows = courseEnrollmentMapper.selectHotCourseStats();
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Integer> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Long courseId = toLong(row.get("courseId"));
            Integer hotCount = toInt(row.get("hotCount"));
            if (courseId != null && hotCount != null) {
                result.put(courseId, hotCount);
            }
        }
        return result;
    }

    /**
     * 批量查询课程
     */
    private List<Course> selectCoursesByIds(Collection<Long> courseIds, boolean onlyPublished) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>().in(Course::getId, courseIds);
        if (onlyPublished) {
            wrapper.eq(Course::getStatus, COURSE_STATUS_PUBLISHED);
        } else {
            wrapper.ne(Course::getStatus, COURSE_STATUS_DELETED);
        }
        return courseMapper.selectList(wrapper);
    }

    /**
     * 构建课程进度快照
     */
    private Map<Long, CourseProgressSnapshot> buildCourseProgressSnapshots(Long studentId, Collection<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> normalizedIds = courseIds.stream().filter(Objects::nonNull).distinct().toList();
        if (normalizedIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<StudyRecord> records = studyRecordMapper.selectList(new LambdaQueryWrapper<StudyRecord>()
                .eq(StudyRecord::getUserId, studentId)
                .in(StudyRecord::getCourseId, normalizedIds));
        Map<Long, Integer> totalResourceMap = queryCourseResourceTotal(normalizedIds);

        Map<Long, Integer> completedMap = new HashMap<>();
        Map<Long, Integer> watchedMap = new HashMap<>();
        Map<Long, LocalDateTime> lastMap = new HashMap<>();
        for (StudyRecord record : records) {
            Long courseId = record.getCourseId();
            if (courseId == null) {
                continue;
            }
            watchedMap.merge(courseId, safeNonNegative(record.getTotalWatchedS()), Integer::sum);
            if (Objects.equals(record.getCompleted(), RECORD_COMPLETED)) {
                completedMap.merge(courseId, 1, Integer::sum);
            }
            LocalDateTime last = lastMap.get(courseId);
            if (record.getLastStudiedAt() != null && (last == null || record.getLastStudiedAt().isAfter(last))) {
                lastMap.put(courseId, record.getLastStudiedAt());
            }
        }

        Map<Long, CourseProgressSnapshot> result = new HashMap<>();
        for (Long courseId : normalizedIds) {
            int total = totalResourceMap.getOrDefault(courseId, 0);
            int completed = completedMap.getOrDefault(courseId, 0);
            int progress = computeProgressPercent(completed, total);
            int watched = watchedMap.getOrDefault(courseId, 0);
            LocalDateTime lastTime = lastMap.get(courseId);
            result.put(courseId, new CourseProgressSnapshot(total, completed, progress, watched, lastTime));
        }
        return result;
    }

    /**
     * 查询课程资源总数
     */
    private Map<Long, Integer> queryCourseResourceTotal(Collection<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Map<String, Object>> rows = resourceMapper.selectMaps(new LambdaQueryWrapper<Resource>()
                .select(Resource::getCourseId, "COUNT(1) AS totalCount")
                .in(Resource::getCourseId, courseIds)
                .groupBy(Resource::getCourseId));
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Integer> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Long courseId = toLong(row.get("courseId"));
            Integer totalCount = toInt(row.get("totalCount"));
            if (courseId != null && totalCount != null) {
                result.put(courseId, totalCount);
            }
        }
        return result;
    }

    /**
     * 加载课程选课状态映射
     */
    private Map<Long, Integer> loadEnrollmentStatusMap(Long studentId, Collection<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<CourseEnrollment> enrollments = courseEnrollmentMapper.selectList(new LambdaQueryWrapper<CourseEnrollment>()
                .eq(CourseEnrollment::getUserId, studentId)
                .in(CourseEnrollment::getCourseId, courseIds));
        return enrollments.stream()
                .collect(Collectors.toMap(CourseEnrollment::getCourseId, CourseEnrollment::getStatus, (left, right) -> left));
    }

    /**
     * 刷新课程完成状态
     */
    private void refreshEnrollmentStatus(Long studentId, Long courseId) {
        CourseEnrollment enrollment = courseEnrollmentMapper.selectOne(new LambdaQueryWrapper<CourseEnrollment>()
                .eq(CourseEnrollment::getUserId, studentId)
                .eq(CourseEnrollment::getCourseId, courseId));
        if (enrollment == null) {
            return;
        }
        int totalResources = resourceMapper.selectCount(new LambdaQueryWrapper<Resource>()
                .eq(Resource::getCourseId, courseId)).intValue();
        int completedResources = studyRecordMapper.selectCount(new LambdaQueryWrapper<StudyRecord>()
                .eq(StudyRecord::getUserId, studentId)
                .eq(StudyRecord::getCourseId, courseId)
                .eq(StudyRecord::getCompleted, RECORD_COMPLETED)).intValue();
        int status = (totalResources > 0 && completedResources >= totalResources)
                ? ENROLLMENT_STATUS_COMPLETED
                : ENROLLMENT_STATUS_LEARNING;
        enrollment.setStatus(status);
        enrollment.setUpdatedAt(LocalDateTime.now());
        courseEnrollmentMapper.updateById(enrollment);
    }

    /**
     * 确保选课关系存在
     */
    private void ensureEnrollment(Long studentId, Long courseId) {
        CourseEnrollment enrollment = courseEnrollmentMapper.selectOne(new LambdaQueryWrapper<CourseEnrollment>()
                .eq(CourseEnrollment::getUserId, studentId)
                .eq(CourseEnrollment::getCourseId, courseId));
        if (enrollment != null) {
            return;
        }
        CourseEnrollment newEnrollment = new CourseEnrollment();
        newEnrollment.setUserId(studentId);
        newEnrollment.setCourseId(courseId);
        newEnrollment.setStatus(ENROLLMENT_STATUS_LEARNING);
        LocalDateTime now = LocalDateTime.now();
        newEnrollment.setEnrolledAt(now);
        newEnrollment.setUpdatedAt(now);
        courseEnrollmentMapper.insert(newEnrollment);
    }

    /**
     * 校验资源属于课程
     */
    private Resource getResourceAndValidateCourse(Long resourceId, Long courseId) {
        Resource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new BaseException(400, "Learning resource does not exist");
        }
        if (!Objects.equals(resource.getCourseId(), courseId)) {
            throw new BaseException(400, "Resource does not belong to current course");
        }
        return resource;
    }

    /**
     * 获取已发布课程
     */
    private Course getPublishedCourse(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null || Objects.equals(course.getStatus(), COURSE_STATUS_DELETED)) {
            throw new BaseException(404, "Course does not exist");
        }
        if (!Objects.equals(course.getStatus(), COURSE_STATUS_PUBLISHED)) {
            throw new BaseException(403, "Course is not published");
        }
        return course;
    }

    /**
     * 转换课程卡片对象
     */
    private CourseCardDto toCourseCard(Course course, Integer enrollmentStatus, CourseProgressSnapshot snapshot) {
        CourseCardDto dto = new CourseCardDto();
        dto.setCourseId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setCoverUrl(course.getCoverUrl());
        dto.setCategory(course.getCategory());
        dto.setDifficulty(course.getDifficulty());
        dto.setEnrollmentStatus(enrollmentStatus);
        dto.setProgressPercent(snapshot.progressPercent());
        dto.setTotalResources(snapshot.totalResources());
        dto.setCompletedResources(snapshot.completedResources());
        dto.setTotalWatchedS(snapshot.totalWatchedS());
        dto.setLastStudiedAt(snapshot.lastStudiedAt());
        return dto;
    }

    /**
     * 计算课程进度百分比
     */
    private int computeProgressPercent(int completedResources, int totalResources) {
        if (totalResources <= 0) {
            return 0;
        }
        return Math.min(100, Math.max(0, completedResources * 100 / totalResources));
    }

    /**
     * 拆分标签字符串
     */
    private Set<String> splitTags(String tags) {
        if (!StringUtils.hasText(tags)) {
            return Collections.emptySet();
        }
        String[] parts = tags.split(",");
        Set<String> result = new HashSet<>();
        for (String part : parts) {
            if (StringUtils.hasText(part)) {
                result.add(part.trim());
            }
        }
        return result;
    }

    /**
     * 统计标签交集数量
     */
    private int countOverlap(Set<String> tags, Set<String> preferredTags) {
        if (tags.isEmpty() || preferredTags.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (String tag : tags) {
            if (preferredTags.contains(tag)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 规范页码
     */
    private int normalizePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    /**
     * 规范页大小
     */
    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return 10;
        }
        return Math.min(size, 50);
    }

    /**
     * 规范推荐数量
     */
    private int normalizeRecommendLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return 6;
        }
        return Math.min(limit, 20);
    }

    /**
     * 安全的非负值处理
     */
    private int safeNonNegative(Integer value) {
        if (value == null || value < 0) {
            return 0;
        }
        return value;
    }

    /**
     * Object 转 Long
     */
    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.valueOf(value.toString());
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Object 转 Integer
     */
    private Integer toInt(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.valueOf(value.toString());
        } catch (Exception ignored) {
            return null;
        }
    }

    private record ScoredCourse(Course course, int hotCount, double score) {
    }

    private record CourseProgressSnapshot(Integer totalResources, Integer completedResources, Integer progressPercent,
                                          Integer totalWatchedS, LocalDateTime lastStudiedAt) {
        private static CourseProgressSnapshot empty() {
            return new CourseProgressSnapshot(0, 0, 0, 0, null);
        }
    }
}
