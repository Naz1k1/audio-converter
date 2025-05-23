<template>
  <div class="convert-page">
    <form class="convert-form" @submit.prevent="handleSubmit">
        <div class="form-group">
          <label>选择视频文件</label>
          <input type="file" accept="video/*" @change="handleFileChange" required>
          <span class="error-text" v-if="fileError">{{ fileError }}</span>
        </div>
      <div class="form-group">
        <label>音频格式:</label>
        <select v-model="format">
          <option value="mp3">mp3</option>
          <option value="aac">aac</option>
          <option value="wav">wav</option>
          <option value="flac">flac</option>
        </select>
      </div>
      <div class="form-group">
        <label>比特率 (32000-320000):</label>
        <input 
          type="number" 
          v-model="bitrate" 
          placeholder="128000"
          min="32000"
          max="320000"
          @input="validateBitrate"
        >
        <span class="error-text" v-if="bitrateError">{{ bitrateError }}</span>
      </div>
      <div class="form-group">
        <label>采样率 (8000-192000):</label>
        <input 
          type="number" 
          v-model="sampleRate" 
          placeholder="44100"
          min="8000"
          max="192000"
          @input="validateSampleRate"
        >
        <span class="error-text" v-if="sampleRateError">{{ sampleRateError }}</span>
      </div>
      <div class="form-group">
        <label>声道数 (1-8):</label>
        <input 
          type="number" 
          v-model="channels" 
          placeholder="2"
          min="1"
          max="8"
          @input="validateChannels"
        >
        <span class="error-text" v-if="channelsError">{{ channelsError }}</span>
      </div>
      <button type="submit" :disabled="loading || hasErrors">开始转换</button>
    </form>

    <div v-if="loading" class="progress">
      正在转换
    </div>

    <div v-if="error" class="error">
      {{ error }}
    </div>

    <div v-if="downloadUrl" class="result">
      <a :href="downloadUrl" download>下载音频</a>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";

const file = ref(null);
const format = ref("mp3");
const bitrate = ref("128000");
const sampleRate = ref("");
const channels = ref("");
const loading = ref(false);
const error = ref("");
const downloadUrl = ref("");

// 错误状态
const fileError = ref("");
const bitrateError = ref("");
const sampleRateError = ref("");
const channelsError = ref("");

const hasErrors = computed(() => {
  return Boolean(fileError.value || bitrateError.value || sampleRateError.value || channelsError.value);
});

function handleFileChange(e) {
  file.value = e.target.files[0];
  fileError.value = file.value ? "" : "请选择要转换的视频文件";
}

function validateBitrate() {
  if (!bitrate.value) return;
  const value = Number(bitrate.value);
  if (value < 32000) {
    bitrateError.value = "比特率不能小于32kbps";
  } else if (value > 320000) {
    bitrateError.value = "比特率不能大于320kbps";
  } else {
    bitrateError.value = "";
  }
}

function validateSampleRate() {
  if (!sampleRate.value) return;
  const value = Number(sampleRate.value);
  if (value < 8000) {
    sampleRateError.value = "采样率不能小于8kHz";
  } else if (value > 192000) {
    sampleRateError.value = "采样率不能大于192kHz";
  } else {
    sampleRateError.value = "";
  }
}

function validateChannels() {
  if (!channels.value) return;
  const value = Number(channels.value);
  if (value < 1) {
    channelsError.value = "声道数不能小于1";
  } else if (value > 8) {
    channelsError.value = "声道数不能大于8";
  } else {
    channelsError.value = "";
  }
}

async function handleSubmit() {
  if (!file.value) {
    fileError.value = "请选择要转换的视频文件";
    return;
  }
  
  loading.value = true;
  error.value = "";
  downloadUrl.value = "";
  
  try {
    const formData = new FormData();
    formData.append("file", file.value);
    let url = `http://localhost:8081/api/converter/video-audio-convert?format=${format.value}`;
    if (bitrate.value) url += `&bitrate=${bitrate.value}`;
    if (sampleRate.value) url += `&sampleRate=${sampleRate.value}`;
    if (channels.value) url += `&channels=${channels.value}`;
    
    const response = await fetch(url, {
      method: "POST",
      body: formData
    });
    
    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.message || "转换失败");
    }
    
    const blob = await response.blob();
    downloadUrl.value = URL.createObjectURL(blob);
  } catch (err) {
    error.value = err.message || "转换失败";
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.convert-page {
  max-width: 400px;
  margin: 40px auto;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  padding: 2rem 1.5rem;
}

.convert-page h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: #2c3e50;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

input[type="file"] {
  border: none;
}

input, select {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
}

button[type="submit"] {
  background: #3498db;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 0.7rem;
  font-size: 1.1rem;
  cursor: pointer;
  transition: background 0.2s;
}

button[disabled] {
  background: #aaa;
  cursor: not-allowed;
}

.progress {
  margin-top: 1rem;
  color: #3498db;
  text-align: center;
}

.error {
  margin-top: 1rem;
  color: #e74c3c;
  text-align: center;
}

.result a {
  color: #27ae60;
  font-weight: bold;
  text-decoration: underline;
}

.error-text {
  color: #e74c3c;
  font-size: 0.9rem;
  margin-top: 0.3rem;
}

.form-group {
  margin-bottom: 1rem;
}
</style>