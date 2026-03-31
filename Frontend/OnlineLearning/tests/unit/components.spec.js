import { shallowMount } from '@vue/test-utils'
import DataTable from '@/components/common/DataTable.vue'
import PageSection from '@/components/common/PageSection.vue'
import StatusTag from '@/components/common/StatusTag.vue'

describe('common components', () => {
  test('DataTable 展示空状态', () => {
    const wrapper = shallowMount(DataTable, {
      props: {
        columns: [{ key: 'name', label: '名称' }],
        rows: [],
      },
    })
    expect(wrapper.text()).toContain('暂无数据')
  })

  test('PageSection 渲染标题', () => {
    const wrapper = shallowMount(PageSection, { props: { title: '测试区块' } })
    expect(wrapper.text()).toContain('测试区块')
  })

  test('StatusTag 渲染状态文案', () => {
    const wrapper = shallowMount(StatusTag, { props: { text: '已发布', type: 'success' } })
    expect(wrapper.text()).toContain('已发布')
  })
})
