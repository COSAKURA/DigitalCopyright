package com.digitalcopyright.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * IO 工具类，用于处理文件和流的操作，包括文件读取、写入、拷贝等功能。
 * @author Sakura
 */
@Slf4j
public class IOUtil {
    // 私有构造方法，防止工具类被实例化
    private IOUtil() {}

    // 定义缓冲区大小为 2048 字节
    private static final int BUF_SIZE = 2048;

    /**
     * 将文件的内容读取为字符串。
     *
     * @param file 目标文件
     * @return 文件内容的字符串形式
     * @throws IOException 如果读取过程中出现 IO 异常
     */
    public static String readAsString(File file) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            copy(in, baos); // 将文件内容拷贝到 ByteArrayOutputStream
            return new String(baos.toByteArray()); // 转为字符串并返回
        }
    }

    /**
     * 读取类路径下的资源文件为字符串。
     *
     * @param resource 资源文件路径
     * @return 资源文件内容的字符串形式，或 null 如果读取失败
     */
    public static String readResourceAsString(String resource) {
        // 使用当前线程的上下文类加载器加载资源
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream in = classLoader.getResourceAsStream(resource)) {
            // 读取输入流内容为字符串
            return readAsString(in);
        } catch (IOException ex) {
            // 记录错误日志
            log.error("Error reading resource", ex);
            return null;
        }
    }

    /**
     * 将输入流的内容读取为字符串。
     *
     * @param inputStream 输入流
     * @return 输入流内容的字符串形式
     * @throws IOException 如果读取过程中出现 IO 异常
     */
    public static String readAsString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 将输入流内容拷贝到 ByteArrayOutputStream
        copy(inputStream, baos);
        // 转为字符串并返回
        return new String(baos.toByteArray());
    }

    /**
     * 将字符串写入到目标文件中。
     *
     * @param target 目标文件
     * @param template 写入的字符串内容
     * @throws IOException 如果写入过程中出现 IO 异常
     */
    public static void writeString(File target, String template) throws IOException {
        ByteArrayInputStream baos = new ByteArrayInputStream(template.getBytes());
        try (FileOutputStream fos = new FileOutputStream(target, false)) {
            // 将字符串内容写入到文件
            copy(baos, fos);
        }
    }

    /**
     * 递归拷贝文件夹内容到目标文件夹。
     *
     * @param srcDir 源文件夹
     * @param destDir 目标文件夹
     * @throws IOException 如果拷贝过程中出现 IO 异常
     */
    public static void copyFolder(File srcDir, final File destDir) throws IOException {
        for (File f : srcDir.listFiles()) {
            File fileCopyTo = new File(destDir, f.getName());
            if (!f.isDirectory()) {
                // 如果是文件，直接拷贝
                copyFile(f, fileCopyTo);
            } else {
                // 如果是文件夹，创建目标文件夹
                if (!fileCopyTo.mkdirs()) {
                    throw new IOException("Dir " + fileCopyTo.getAbsolutePath() + " create failed");
                }
                // 递归拷贝子文件夹
                copyFolder(f, fileCopyTo);
            }
        }
    }

    /**
     * 拷贝单个文件到目标文件。
     *
     * @param src 源文件
     * @param tgt 目标文件
     * @throws IOException 如果拷贝过程中出现 IO 异常
     */
    public static void copyFile(File src, File tgt) throws IOException {
        try (FileInputStream fis = new FileInputStream(src);
             FileOutputStream fos = new FileOutputStream(tgt, false)) {
            // 调用通用的拷贝方法
            copy(fis, fos);
        }
    }

    /**
     * 通用的流拷贝方法，将输入流拷贝到输出流。
     *
     * @param is 输入流
     * @param os 输出流
     * @throws IOException 如果拷贝过程中出现 IO 异常
     */
    public static void copy(InputStream is, OutputStream os) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(is);
             BufferedOutputStream bos = new BufferedOutputStream(os)) {
            byte[] buf = new byte[BUF_SIZE];
            int n;
            // 持续读取数据直到结束
            while ((n = bis.read(buf)) != -1) {
                // 写入数据
                bos.write(buf, 0, n);
            }
            // 刷新缓冲区
            bos.flush();
        }
    }

    /**
     * 删除指定文件或文件夹（包括子文件和子文件夹）。
     *
     * @param item 需要删除的文件或文件夹
     */
    public static void removeItem(File item) {
        // 如果是文件，直接删除
        if (!item.isDirectory()) {
            item.delete();
            return;
        }

        // 如果是文件夹，递归删除子文件和子文件夹
        for (File subItem : item.listFiles()) {
            removeItem(subItem);
        }
        // 删除空文件夹
        item.delete();
    }
}
