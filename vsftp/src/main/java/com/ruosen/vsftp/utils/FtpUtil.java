package com.ruosen.vsftp.utils;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class FtpUtil {
    /**
     * FTP服务器ip
     */
    @Value("${ftp.host}")
    private String host;
    /**
     * FTP服务器端口
     */
    @Value("${ftp.port}")
    private int port;
    /**
     * FTP登录账号
     */
    @Value("${ftp.userName}")
    private String userName;
    /**
     * FTP登录密码
     */
    @Value("${ftp.password}")
    private String password;

    /**
     * Description: 向FTP服务器上传文件
     *
     * @param basePath FTP服务器基础目录,/home/ftpuser/images
     * @param filePath FTP服务器文件存放路径。例如分日期存放：/2018/05/28。文件的路径为basePath+filePath
     * @param fileName 上传到FTP服务器上的文件名
     * @param input    输入流
     */
    public String uploadFile(String basePath, String filePath, String fileName, InputStream input) {
        FTPClient ftp = new FTPClient();
        try {
            int reply;
            // 连接FTP服务器
            ftp.connect(host, port);
            // 如果采用默认端口，可以使用ftp.connect(host)的方式直接连接FTP服务器
            // 登录
            ftp.login(userName, password);
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return "图片服务器发生异常！";
            }

            ftp.setControlEncoding("UTF-8");
            FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_NT);
            conf.setServerLanguageCode("zh");


            //切换到上传目录
            if (!ftp.changeWorkingDirectory(basePath + filePath)) {
                //如果目录不存在创建目录
                String[] dirs = filePath.split("/");
                String tempPath = basePath;
                for (String dir : dirs) {
                    if (null == dir || "".equals(dir)) {
                        continue;
                    }
                    tempPath += "/" + dir;
                    boolean b1 = ftp.changeWorkingDirectory(tempPath);
                    if (!b1) {
                        boolean b = ftp.makeDirectory(tempPath);
                        if (!b) {
                            return "图片服务器发生异常！";
                        } else {
                            ftp.changeWorkingDirectory(tempPath);
                        }
                    }
                }
            }
            //设置为被动模式
            ftp.enterLocalPassiveMode();
            //设置上传文件的类型为二进制类型
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            //上传文件
            String fileNameNew = new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
            if (!ftp.storeFile(fileNameNew, input)) {
                return "图片服务器发生异常！";
            }
            input.close();
            ftp.logout();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                }
            }
        }
        return filePath + "/" + fileName;
    }
}
