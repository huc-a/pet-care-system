<template><router-view /></template>
<style>
:root { --pet-primary: #f97316; }
* { box-sizing: border-box; }
body { margin: 0; background: #fff7ed; font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif; color: #431407; }
.el-button--primary { background: linear-gradient(135deg, #f97316, #fb923c) !important; border: none !important; color: #fff !important; }
.el-button--primary.is-plain { background: #fff7ed !important; color: #ea580c !important; border: 1px solid #fdba74 !important; }
.el-card { border-radius: 16px !important; border: none !important; box-shadow: 0 4px 20px rgba(249,115,22,.08) !important; }
.el-table { border-radius: 12px; overflow: hidden; }
.el-dialog { border-radius: 18px !important; }
.page-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.page-head h2 { margin: 0; font-size: 20px; }
.page-head .sub { color: #9a3412; opacity: .65; font-size: 13px; margin-top: 4px; }
.stat-card { display: flex; gap: 14px; align-items: center; }
.stat-icon { width: 52px; height: 52px; border-radius: 14px; display: flex; align-items: center; justify-content: center; font-size: 26px; flex-shrink: 0; }
.stat-num { font-size: 24px; font-weight: 700; }
.stat-label { color: #78716c; font-size: 13px; }
</style>
