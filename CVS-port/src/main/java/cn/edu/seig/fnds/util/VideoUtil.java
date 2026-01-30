package cn.edu.seig.fnds.util;

import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.info.MultimediaInfo;
import ws.schild.jave.info.VideoInfo;

import java.io.File;
import java.net.URL;

/**
 * 视频工具类
 * 用于获取视频信息（时长、分辨率等）
 * 
 * @author lazzydu
 */
public class VideoUtil {

    /**
     * 获取视频时长（秒）
     * 
     * @param videoPath 视频文件路径或URL
     * @return 视频时长（秒），如果获取失败返回0
     */
    public static int getVideoDuration(String videoPath) {
        try {
            File videoFile;
            
            // 判断是URL还是本地文件路径
            if (videoPath.startsWith("http://") || videoPath.startsWith("https://")) {
                // 如果是URL，先下载到临时文件
                // 注意：这里简化处理，实际生产环境可能需要更复杂的逻辑
                return getVideoDurationFromUrl(videoPath);
            } else {
                // 本地文件
                videoFile = new File(videoPath);
                if (!videoFile.exists()) {
                    System.err.println("视频文件不存在: " + videoPath);
                    return 0;
                }
            }
            
            // 使用JAVE获取视频信息
            MultimediaObject multimediaObject = new MultimediaObject(videoFile);
            MultimediaInfo info = multimediaObject.getInfo();
            
            // 获取时长（毫秒）并转换为秒
            long durationMillis = info.getDuration();
            int durationSeconds = (int) (durationMillis / 1000);
            
            System.out.println("视频时长: " + durationSeconds + "秒 (" + formatDuration(durationSeconds) + ")");
            
            return durationSeconds;
            
        } catch (Exception e) {
            System.err.println("获取视频时长失败: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 从URL获取视频时长
     * 
     * @param videoUrl 视频URL
     * @return 视频时长（秒）
     */
    private static int getVideoDurationFromUrl(String videoUrl) {
        try {
            // 对于URL，我们可以直接使用MultimediaObject
            // JAVE支持从URL读取
            URL url = new URL(videoUrl);
            MultimediaObject multimediaObject = new MultimediaObject(url);
            MultimediaInfo info = multimediaObject.getInfo();
            
            long durationMillis = info.getDuration();
            int durationSeconds = (int) (durationMillis / 1000);
            
            return durationSeconds;
            
        } catch (Exception e) {
            System.err.println("从URL获取视频时长失败: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * 获取视频详细信息
     * 
     * @param videoPath 视频文件路径
     * @return 视频信息对象
     */
    public static VideoMetadata getVideoMetadata(String videoPath) {
        try {
            File videoFile = new File(videoPath);
            if (!videoFile.exists()) {
                return null;
            }
            
            MultimediaObject multimediaObject = new MultimediaObject(videoFile);
            MultimediaInfo info = multimediaObject.getInfo();
            VideoInfo videoInfo = info.getVideo();
            
            VideoMetadata metadata = new VideoMetadata();
            metadata.setDuration((int) (info.getDuration() / 1000));
            
            if (videoInfo != null) {
                metadata.setWidth(videoInfo.getSize().getWidth());
                metadata.setHeight(videoInfo.getSize().getHeight());
                metadata.setBitRate(videoInfo.getBitRate());
                metadata.setFrameRate(videoInfo.getFrameRate());
            }
            
            return metadata;
            
        } catch (Exception e) {
            System.err.println("获取视频元数据失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 格式化时长为 HH:MM:SS 格式
     * 
     * @param seconds 秒数
     * @return 格式化的时长字符串
     */
    public static String formatDuration(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%02d:%02d", minutes, secs);
        }
    }
    
    /**
     * 视频元数据类
     */
    public static class VideoMetadata {
        private int duration;      // 时长（秒）
        private int width;         // 宽度
        private int height;        // 高度
        private int bitRate;       // 比特率
        private float frameRate;   // 帧率
        
        // Getters and Setters
        public int getDuration() { return duration; }
        public void setDuration(int duration) { this.duration = duration; }
        
        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }
        
        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }
        
        public int getBitRate() { return bitRate; }
        public void setBitRate(int bitRate) { this.bitRate = bitRate; }
        
        public float getFrameRate() { return frameRate; }
        public void setFrameRate(float frameRate) { this.frameRate = frameRate; }
    }
}

