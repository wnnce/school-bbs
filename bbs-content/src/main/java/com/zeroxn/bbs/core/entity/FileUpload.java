package com.zeroxn.bbs.core.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传表 实体类。
 *
 * @author lisang
 * @since 2023-10-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "bbs_file_upload")
public class FileUpload implements Serializable {

    /**
     * id 主键 自增
     */
    @Id(keyType = KeyType.Auto)
    private Integer id;

    /**
     * 文件32位MD5 非空
     */
    private String md5;

    /**
     * 文件原始名称 非空
     */
    private String originName;

    /**
     * 文件保存的名称 非空
     */
    private String fileName;

    /**
     * 文件大小
     */
    private Long size;

    /**
     * 文件访问Url
     */
    private String url;

    /**
     * 文件上传时间
     */
    @Column(onInsertValue = "current_timestamp")
    private Timestamp uploadTime;

    public FileUpload(String md5, String originName, String fileName, Long size, String url) {
        this.md5 = md5;
        this.originName = originName;
        this.fileName = fileName;
        this.size = size;
        this.url = url;
    }
}
