<template>
  <footer class="layout-footer">
    <div class="footer-content">
      <p>&copy; 2025 图片管理系统.</p>
    </div>
  </footer>
</template>

<script setup></script>

<style scoped>
.layout-footer {
  background: #2c3e50;
  color: #ecf0f1;
  padding: 30px 20px;
  margin-top: auto;
}

.footer-content {
  max-width: 1400px;
  margin: 0 auto;
  text-align: center;
}

.footer-content p {
  margin: 0 0 10px 0;
  font-size: 14px;
}

.footer-links {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}

.footer-links a {
  color: #ecf0f1;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-links a:hover {
  color: #3498db;
}

.divider {
  color: #7f8c8d;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .footer-content p {
    font-size: 12px;
  }

  .footer-links {
    font-size: 12px;
    gap: 5px;
  }
}
</style>

