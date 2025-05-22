<template>
  <div class="convert-page">
    <form class="convert-form" @submit.prevent="handleSubmit">
        <div class="form-group">
          <label>选择视频文件</label>
          <input type="file" accept="video/*" @change="handleFileChange" required>
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
        <label>比特率:</label>
        <input type="number" v-model="bitrate" placeholder="192000 (可选)">
      </div>
      <div class="form-group">
        <label>采样率:</label>
        <input type="number" v-model="sampleRate" placeholder="44100 (可选)">
      </div>
      <div class="form-group">
        <label>声道数:</label>
        <input type="number" v-model="channels" placeholder="2 (可选)">
      </div>
      <button type="submit" :disabled="loading">开始转换</button>
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
import { ref } from "vue";

const file = ref(null);
const format = ref("mp3");
const bitrate = ref("");
const sampleRate = ref("");
const channels = ref("");
const loading = ref(false);
const error = ref("");
const downloadUrl = ref("");

function handleFileChange(e) {
  file.value = e.target.files[0];
}

async function handleSubmit() {
  if (!file.value)
    return;
  loading.value = true;
  error.value = "";
  downloadUrl.value = "";
  try {
    const formData = new FormData();
    formData.append("file",file.value);
    let url = `http://localhost:8081/video-audio-convert?format=${format.value}`;
    if (bitrate.value) url += `&bitrate=${bitrate.value}`;
    if (sampleRate.value) url += `&sampleRate=${sampleRate.value}`;
    if (channels.value) url += `&channels=${channels.value}`;
    const response = await fetch(url,{
      method: "POST",
      body: formData
    });
    if (!response.ok)
      throw new Error("转换失败");
    const blob = await response.blob();
    downloadUrl.value = URL.createObjectURL(blob);
  } catch (error) {
    error.value = error.message || "转换失败"
  } finally {
    loading.value = false;
  }
}
</script>