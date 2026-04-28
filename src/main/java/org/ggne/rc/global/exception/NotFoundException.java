package org.ggne.rc.global.exception;

public class NotFoundException extends BusinessException {

    public NotFoundException(String resource) {
        super(resource + "을(를) 찾을 수 없습니다.", 404);
    }
}
