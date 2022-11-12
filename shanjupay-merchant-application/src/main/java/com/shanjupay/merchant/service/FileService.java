package com.shanjupay.merchant.service;

/**
 * @author 小郭
 * @version 1.0
 */

import java.sql.BatchUpdateException;

/**
 * <P>
 * 文件服务
 * </p>
 *
 */
public interface FileService {

    /**
     * 上传文件
     * @param bytes 文件字节
     * @param fileName 文件名称
     * @return 文件下载路径
     * @throws BatchUpdateException
     */
    String upload(byte[] bytes,String fileName) throws BatchUpdateException;
}
