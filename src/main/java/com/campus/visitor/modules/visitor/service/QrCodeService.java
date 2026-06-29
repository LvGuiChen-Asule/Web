package com.campus.visitor.modules.visitor.service;

import com.campus.visitor.modules.visitor.service.dto.QrVerifyResult;

public interface QrCodeService {
    String generate(Long appointmentId);

    QrVerifyResult verify(String qrContent, boolean checkUsed);
}
