<template>
  <div>
    <v-chart :forceFit="true" :height="height" :data="data">
      <v-coord type="rect" direction="LB"/>
      <v-tooltip/>
      <v-legend/>
      <v-axis dataKey="State" :label="label"/>
      <v-stack-bar position="State*Number" color="Process state"/>
    </v-chart>
  </div>

</template>

<script>
const DataSet = require('@antv/data-set')

export default {
  name: 'StackBar',
  props: {
    dataSource: {
      type: Array,
      required: true,
      default: () => [
        { 'State': 'leave', 'flow': 25, 'archived': 18 },
        { 'State': 'evection', 'flow': 30, 'archived': 20 },
        { 'State': 'overtime', 'flow': 38, 'archived': 42 },
        { 'State': 'useCar', 'flow': 51, 'archived': 67 }
      ]
    },
    height: {
      type: Number,
      default: 254
    }
  },
  data() {
    return {
      label: { offset: 12 }
    }
  },
  computed: {
    data() {
      const dv = new DataSet.View().source(this.dataSource)
      dv.transform({
        type: 'fold',
        fields: ['flow', 'archived'],
        key: 'Process state',
        value: 'State*Number',
        retains: ['State']
      })
      return dv.rows
    }
  }
}
</script>