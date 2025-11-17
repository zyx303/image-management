<template>
  <div class="slideshow-container">
    <div v-if="images.length > 0" class="slideshow">
      <!-- 主图片显示 -->
      <div class="main-slide">
        <img
          :src="getImageUrl(currentImage.filePath || currentImage.thumbnailPath)"
          :alt="currentImage.fileName"
          class="slide-image"
        />
        <div class="slide-info">
          <h3>{{ currentImage.fileName }}</h3>
          <p v-if="currentImage.description">{{ currentImage.description }}</p>
        </div>
      </div>

      <!-- 控制按钮 -->
      <div class="slide-controls">
        <el-button
          :icon="ArrowLeft"
          circle
          size="large"
          class="prev-btn"
          @click="prevSlide"
        />
        <el-button
          :icon="isPlaying ? VideoPause : VideoPlay"
          circle
          size="large"
          class="play-btn"
          @click="togglePlay"
        />
        <el-button
          :icon="ArrowRight"
          circle
          size="large"
          class="next-btn"
          @click="nextSlide"
        />
      </div>

      <!-- 进度指示器 -->
      <div class="slide-indicators">
        <span
          v-for="(img, index) in images"
          :key="img.id"
          :class="['indicator', { active: index === currentIndex }]"
          @click="goToSlide(index)"
        />
      </div>

      <!-- 缩略图导航 -->
      <div class="thumbnails">
        <div
          v-for="(img, index) in images"
          :key="img.id"
          :class="['thumbnail', { active: index === currentIndex }]"
          @click="goToSlide(index)"
        >
          <img
            :src="getImageUrl(img.thumbnailPath)"
            :alt="img.fileName"
          />
        </div>
      </div>

      <!-- 设置面板 -->
      <div class="slideshow-settings">
        <el-form label-width="80px" size="small">
          <el-form-item label="间隔时间">
            <el-slider
              v-model="interval"
              :min="1"
              :max="10"
              :marks="{ 1: '1s', 5: '5s', 10: '10s' }"
              @change="handleIntervalChange"
            />
          </el-form-item>
          <el-form-item label="过渡效果">
            <el-select v-model="transition" placeholder="选择过渡效果">
              <el-option label="淡入淡出" value="fade" />
              <el-option label="滑动" value="slide" />
              <el-option label="缩放" value="zoom" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <el-empty v-else description="没有图片可展示" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import {
  ArrowLeft,
  ArrowRight,
  VideoPlay,
  VideoPause
} from '@element-plus/icons-vue'

const props = defineProps({
  images: {
    type: Array,
    default: () => []
  },
  autoPlay: {
    type: Boolean,
    default: false
  },
  defaultInterval: {
    type: Number,
    default: 3
  }
})

const currentIndex = ref(0)
const isPlaying = ref(props.autoPlay)
const interval = ref(props.defaultInterval)
const transition = ref('fade')
let timer = null

const currentImage = computed(() => {
  return props.images[currentIndex.value] || {}
})

onMounted(() => {
  if (isPlaying.value) {
    startAutoPlay()
  }
})

onUnmounted(() => {
  stopAutoPlay()
})

watch(() => props.images, () => {
  if (currentIndex.value >= props.images.length) {
    currentIndex.value = 0
  }
})

const nextSlide = () => {
  currentIndex.value = (currentIndex.value + 1) % props.images.length
}

const prevSlide = () => {
  currentIndex.value =
    (currentIndex.value - 1 + props.images.length) % props.images.length
}

const goToSlide = (index) => {
  currentIndex.value = index
}

const togglePlay = () => {
  isPlaying.value = !isPlaying.value
  if (isPlaying.value) {
    startAutoPlay()
  } else {
    stopAutoPlay()
  }
}

const startAutoPlay = () => {
  stopAutoPlay()
  timer = setInterval(() => {
    nextSlide()
  }, interval.value * 1000)
}

const stopAutoPlay = () => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

const handleIntervalChange = () => {
  if (isPlaying.value) {
    startAutoPlay()
  }
}

const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/files/${path}`
}
</script>

<style scoped>
.slideshow-container {
  width: 100%;
  min-height: 500px;
}

.slideshow {
  position: relative;
  background: #000;
  border-radius: 12px;
  overflow: hidden;
}

.main-slide {
  position: relative;
  width: 100%;
  height: 60vh;
  min-height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.slide-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  animation: fadeIn 0.5s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.slide-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);
  color: white;
  padding: 30px 20px 20px;
}

.slide-info h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
}

.slide-info p {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

.slide-controls {
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  transform: translateY(-50%);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  pointer-events: none;
}

.slide-controls .el-button {
  pointer-events: all;
  background: rgba(255, 255, 255, 0.9);
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.play-btn {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
}

.slide-indicators {
  position: absolute;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
  z-index: 10;
}

.indicator {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: all 0.3s;
}

.indicator.active {
  background: white;
  width: 30px;
  border-radius: 5px;
}

.thumbnails {
  display: flex;
  gap: 10px;
  padding: 15px;
  background: rgba(0, 0, 0, 0.5);
  overflow-x: auto;
}

.thumbnail {
  flex-shrink: 0;
  width: 80px;
  height: 60px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  opacity: 0.6;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.thumbnail:hover {
  opacity: 1;
}

.thumbnail.active {
  opacity: 1;
  border-color: #409eff;
  box-shadow: 0 0 10px rgba(64, 158, 255, 0.5);
}

.thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.slideshow-settings {
  background: white;
  padding: 20px;
  border-radius: 0 0 12px 12px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .main-slide {
    height: 50vh;
    min-height: 300px;
  }

  .slide-info h3 {
    font-size: 16px;
  }

  .slide-info p {
    font-size: 12px;
  }

  .slide-controls {
    padding: 0 10px;
  }

  .slide-controls .el-button {
    width: 40px;
    height: 40px;
  }

  .thumbnail {
    width: 60px;
    height: 45px;
  }

  .slideshow-settings {
    padding: 15px;
  }
}
</style>

