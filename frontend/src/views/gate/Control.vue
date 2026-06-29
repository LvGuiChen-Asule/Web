<template>
  <div class="gate-control">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>闸机控制</span>
        </div>
      </template>
      
      <div class="control-panel">
        <el-tabs v-model="activeTab" class="demo-tabs">
          <el-tab-pane label="入校核验" name="check-in">
            <div class="action-box">
              <div class="input-row">
                <el-input
                  v-model="checkInQr"
                  placeholder="请扫描二维码或输入内容"
                  clearable
                  @keyup.enter="handleCheckIn"
                >
                  <template #prefix>
                    <el-icon><Scissor /></el-icon>
                  </template>
                </el-input>
                <el-button type="primary" plain @click="openScanner('check-in')">
                  <el-icon><VideoCamera /></el-icon>
                  扫码
                </el-button>
              </div>
              <el-button type="primary" size="large" :loading="loading" @click="handleCheckIn" class="mt-20 w-full">
                核验并入校
              </el-button>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="离校核验" name="check-out">
            <div class="action-box">
              <div class="input-row">
                <el-input
                  v-model="checkOutQr"
                  placeholder="请扫描二维码或输入内容"
                  clearable
                  @keyup.enter="handleCheckOut"
                >
                  <template #prefix>
                    <el-icon><Scissor /></el-icon>
                  </template>
                </el-input>
                <el-button type="success" plain @click="openScanner('check-out')">
                  <el-icon><VideoCamera /></el-icon>
                  扫码
                </el-button>
              </div>
              <el-button type="success" size="large" :loading="loading" @click="handleCheckOut" class="mt-20 w-full">
                核验并离校
              </el-button>
            </div>
          </el-tab-pane>
        </el-tabs>

        <div class="result-display" v-if="lastResult">
          <el-alert
            :title="lastResult.title"
            :type="lastResult.type"
            :description="lastResult.message"
            show-icon
            :closable="false"
          />
        </div>
      </div>
    </el-card>

    <el-dialog v-model="scannerVisible" title="扫码" width="560px" @closed="stopScanner">
      <div class="scanner-body">
        <div v-if="scannerError" class="scanner-error">
          <el-alert :title="scannerError" type="error" show-icon :closable="false" />
        </div>
        <div v-else class="scanner-video-wrap">
          <div class="scanner-toolbar">
            <el-select v-model="selectedDeviceId" placeholder="选择摄像头" clearable style="width: 100%">
              <el-option v-for="d in videoDevices" :key="d.deviceId" :label="d.label" :value="d.deviceId" />
            </el-select>
            <el-button @click="refreshDevices" :loading="deviceLoading" plain>
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
          <video ref="videoRef" class="scanner-video" autoplay playsinline muted></video>
          <div class="scanner-tip">对准二维码自动识别</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="scannerVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onBeforeUnmount, watch } from 'vue'
import { checkIn, checkOut } from '../../api/gate'
import { verifyQrCode } from '../../api/qrcode'
import { Scissor, VideoCamera, Refresh } from '@element-plus/icons-vue'

const activeTab = ref('check-in')
const checkInQr = ref('')
const checkOutQr = ref('')
const loading = ref(false)
const lastResult = ref<any>(null)

const scannerVisible = ref(false)
const scannerError = ref('')
const scanTarget = ref<'check-in' | 'check-out'>('check-in')
const videoRef = ref<HTMLVideoElement | null>(null)
const videoDevices = ref<Array<{ deviceId: string; label: string }>>([])
const selectedDeviceId = ref<string>('')
const deviceLoading = ref(false)
let mediaStream: MediaStream | null = null
let scanTimer: number | null = null
let detector: any | null = null
let zxingReader: any | null = null
let zxingControls: any | null = null

const openScanner = async (target: 'check-in' | 'check-out') => {
  scanTarget.value = target
  scannerError.value = ''
  scannerVisible.value = true
  await refreshDevices()
  await startScanner()
}

const refreshDevices = async () => {
  deviceLoading.value = true
  try {
    try {
      const tmp = await navigator.mediaDevices.getUserMedia({ video: true, audio: false })
      tmp.getTracks().forEach((t) => t.stop())
    } catch {
    }
    const devices = await navigator.mediaDevices.enumerateDevices()
    const videos = devices.filter((d) => d.kind === 'videoinput')
    videoDevices.value = videos.map((d, idx) => ({
      deviceId: d.deviceId,
      label: d.label || `摄像头 ${idx + 1}`
    }))

    const saved = localStorage.getItem('gate_scanner_device') || ''
    const hasSaved = saved && videoDevices.value.some((d) => d.deviceId === saved)
    if (!selectedDeviceId.value || !videoDevices.value.some((d) => d.deviceId === selectedDeviceId.value)) {
      if (hasSaved) {
        selectedDeviceId.value = saved
      } else {
        const prefer = videoDevices.value.find((d) => /obs|virtual|虚拟/i.test(d.label))
        selectedDeviceId.value = (prefer || videoDevices.value[0])?.deviceId || ''
      }
    }
  } catch (e: any) {
    scannerError.value = e?.message || '无法获取摄像头列表'
  } finally {
    deviceLoading.value = false
  }
}

const ensureZxing = async () => {
  if (zxingReader) return
  const mod: any = await import('@zxing/browser')
  zxingReader = new mod.BrowserQRCodeReader()
}

const startScanner = async () => {
  try {
    stopScanner()
    const video = videoRef.value
    if (!video) return

    if ('BarcodeDetector' in window) {
      detector = detector || new (window as any).BarcodeDetector({ formats: ['qr_code'] })
      mediaStream = await navigator.mediaDevices.getUserMedia({
        video: selectedDeviceId.value ? { deviceId: { exact: selectedDeviceId.value } } : { facingMode: 'environment' },
        audio: false
      })
      video.srcObject = mediaStream
      await video.play()

      const tick = async () => {
        if (!scannerVisible.value) return
        try {
          const codes = await detector.detect(video)
          if (codes && codes.length > 0) {
            const value = codes[0]?.rawValue || ''
            if (value) {
              if (scanTarget.value === 'check-in') checkInQr.value = value
              else checkOutQr.value = value
              scannerVisible.value = false
              stopScanner()
              return
            }
          }
        } catch (e: any) {
          scannerError.value = e?.message || '扫码失败'
          return
        }
        scanTimer = window.setTimeout(tick, 200)
      }
      scanTimer = window.setTimeout(tick, 0)
      return
    }

    await ensureZxing()
    const deviceId = selectedDeviceId.value || undefined
    zxingControls = await zxingReader.decodeFromVideoDevice(deviceId, video, (result: any, _error: any) => {
      if (!scannerVisible.value) return
      const value = result?.getText?.() || ''
      if (value) {
        if (scanTarget.value === 'check-in') checkInQr.value = value
        else checkOutQr.value = value
        scannerVisible.value = false
        stopScanner()
      }
    })
  } catch (e: any) {
    scannerError.value = e?.message || '无法打开摄像头'
  }
}

const stopScanner = () => {
  if (scanTimer != null) {
    window.clearTimeout(scanTimer)
    scanTimer = null
  }
  if (mediaStream) {
    mediaStream.getTracks().forEach((t) => t.stop())
    mediaStream = null
  }
  if (zxingControls) {
    try {
      zxingControls.stop()
    } catch {
    }
    zxingControls = null
  }
  const video = videoRef.value
  if (video) {
    video.srcObject = null
  }
}

watch(
  () => selectedDeviceId.value,
  (val) => {
    if (!scannerVisible.value) return
    if (val) localStorage.setItem('gate_scanner_device', val)
    startScanner()
  }
)

const handleCheckIn = async () => {
  if (!checkInQr.value) return
  loading.value = true
  lastResult.value = null
  try {
    const vr: any = await verifyQrCode(checkInQr.value, true)
    if (vr.data.result !== 'VALID') {
      lastResult.value = {
        title: '拒绝通行',
        type: 'error',
        message: vr.data.message || '核验失败'
      }
      return
    }
    await checkIn(checkInQr.value)
    lastResult.value = {
      title: '允许通行',
      type: 'success',
      message: '入校成功。'
    }
    checkInQr.value = ''
  } catch (error: any) {
    lastResult.value = {
      title: '拒绝通行',
      type: 'error',
      message: error.message || '核验失败'
    }
  } finally {
    loading.value = false
  }
}

const handleCheckOut = async () => {
  if (!checkOutQr.value) return
  loading.value = true
  lastResult.value = null
  try {
    await checkOut(checkOutQr.value)
    lastResult.value = {
      title: '再见',
      type: 'success',
      message: '离校成功。'
    }
    checkOutQr.value = ''
  } catch (error: any) {
    lastResult.value = {
      title: '错误',
      type: 'error',
      message: error.message || '操作失败'
    }
  } finally {
    loading.value = false
  }
}

onBeforeUnmount(() => {
  stopScanner()
})
</script>

<style scoped lang="scss">
.gate-control {
  max-width: 600px;
  margin: 0 auto;
  
  .control-panel {
    padding: 20px 0;
    
    .action-box {
      padding: 20px 0;
    }
    

    .input-row {
      display: flex;
      gap: 10px;
      align-items: center;
    }
    .mt-20 {
      margin-top: 20px;
    }
    
    .w-full {
      width: 100%;
    }
    
    .result-display {
      margin-top: 30px;
    }
  }
}

.scanner-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.scanner-video-wrap {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.scanner-toolbar {
  display: flex;
  gap: 10px;
  align-items: center;
}

.scanner-video {
  width: 100%;
  max-height: 360px;
  background: #000;
  border-radius: 6px;
  object-fit: cover;
}

.scanner-tip {
  font-size: 12px;
  color: #909399;
}
</style>
