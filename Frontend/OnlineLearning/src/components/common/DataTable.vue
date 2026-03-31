<template>
  <div class="data-table">
    <table>
      <thead>
        <tr>
          <th v-for="column in columns" :key="column.key">{{ column.label }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="loading">
          <td :colspan="columns.length">加载中...</td>
        </tr>
        <tr v-else-if="error">
          <td :colspan="columns.length">{{ error }}</td>
        </tr>
        <tr v-else-if="rows.length === 0">
          <td :colspan="columns.length">暂无数据</td>
        </tr>
        <tr v-for="row in rows" :key="row[idKey]">
          <td v-for="column in columns" :key="`${row[idKey]}-${column.key}`">
            {{ row[column.key] }}
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
/**
 * 通用数据表格组件，支持加载、空态、错误态。
 */
defineProps({
  columns: {
    type: Array,
    required: true,
  },
  rows: {
    type: Array,
    default: () => [],
  },
  idKey: {
    type: String,
    default: 'id',
  },
  loading: {
    type: Boolean,
    default: false,
  },
  error: {
    type: String,
    default: '',
  },
})
</script>

<style lang="scss" scoped>
.data-table {
  overflow: auto;

  table {
    width: 100%;
    border-collapse: collapse;
  }

  th,
  td {
    text-align: left;
    padding: $space-3;
    border-bottom: 1px solid $color-border;
    font-size: $font-size-sm;
  }

  th {
    color: $color-text-secondary;
    font-weight: $font-weight-semibold;
  }
}
</style>
