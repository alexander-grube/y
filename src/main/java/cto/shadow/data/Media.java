package cto.shadow.data;

import com.alibaba.fastjson2.annotation.JSONCompiled;

import java.time.OffsetDateTime;

@JSONCompiled
public record Media(
        String name,
        String url,
        MediaType type,
        OffsetDateTime uploadedAt
) {
    public Media {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Media name cannot be null or blank");
        }
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Media URL cannot be null or blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("Media type cannot be null");
        }
        if (uploadedAt == null) {
            throw new IllegalArgumentException("Upload date cannot be null");
        }
    }
}
