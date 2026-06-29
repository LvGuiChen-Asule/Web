package com.campus.visitor.modules.visitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.visitor.common.exception.BizException;
import com.campus.visitor.modules.visitor.dto.QrCodePayload;
import com.campus.visitor.modules.visitor.dto.AppointmentItemView;
import com.campus.visitor.modules.visitor.entity.AccessRecord;
import com.campus.visitor.modules.visitor.entity.Area;
import com.campus.visitor.modules.visitor.entity.AppointmentItem;
import com.campus.visitor.modules.visitor.entity.VisAppointment;
import com.campus.visitor.modules.visitor.enums.AppointmentStatus;
import com.campus.visitor.modules.visitor.mapper.AccessRecordMapper;
import com.campus.visitor.modules.visitor.mapper.AreaMapper;
import com.campus.visitor.modules.visitor.mapper.AppointmentItemMapper;
import com.campus.visitor.modules.visitor.mapper.VisAppointmentMapper;
import com.campus.visitor.modules.visitor.service.QrCodeService;
import com.campus.visitor.modules.visitor.service.dto.QrVerifyResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QrCodeServiceImpl implements QrCodeService {
    private final VisAppointmentMapper appointmentMapper;
    private final AccessRecordMapper accessRecordMapper;
    private final AreaMapper areaMapper;
    private final AppointmentItemMapper appointmentItemMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generate(Long appointmentId) {
        VisAppointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new BizException(404, "预约不存在");
        }
        if (!AppointmentStatus.APPROVED.name().equals(appointment.getStatus())) {
            throw new BizException(400, "预约未通过审批");
        }
        QrCodePayload payload = new QrCodePayload();
        payload.setAppointmentId(appointment.getId());
        payload.setVisitorName(appointment.getVisitorName());
        payload.setVisitorPhone(appointment.getVisitorPhone());
        payload.setValidStartTime(appointment.getVisitStartTime());
        payload.setValidEndTime(appointment.getVisitEndTime());

        String content;
        try {
            content = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new BizException(500, "生成二维码数据失败");
        }

        String base64 = toPngBase64(content, 360, 360);
        appointment.setQrcodeUrl(base64);
        appointmentMapper.updateById(appointment);
        return base64;
    }

    @Override
    public QrVerifyResult verify(String qrContent, boolean checkUsed) {
        if (qrContent == null || qrContent.isBlank()) {
            throw new BizException(400, "二维码内容不能为空");
        }
        QrCodePayload payload;
        try {
            payload = objectMapper.readValue(qrContent, QrCodePayload.class);
        } catch (JsonProcessingException e) {
            throw new BizException(400, "二维码内容不合法");
        }
        if (payload.getAppointmentId() == null) {
            throw new BizException(400, "二维码内容不合法");
        }

        VisAppointment appointment = appointmentMapper.selectById(payload.getAppointmentId());
        if (appointment == null) {
            QrVerifyResult r = new QrVerifyResult();
            r.setResult("INVALID");
            r.setMessage("预约不存在");
            r.setAppointmentId(payload.getAppointmentId());
            return r;
        }

        QrVerifyResult r = new QrVerifyResult();
        r.setAppointmentId(appointment.getId());
        r.setAreaId(appointment.getAreaId());
        if (appointment.getAreaId() != null) {
            Area area = areaMapper.selectById(appointment.getAreaId());
            if (area != null && Boolean.TRUE.equals(area.getIsActive())) {
                r.setAreaName(area.getAreaName());
            }
        }
        List<AppointmentItem> items = appointmentItemMapper.selectList(new LambdaQueryWrapper<AppointmentItem>()
                .eq(AppointmentItem::getAppointmentId, appointment.getId())
                .orderByAsc(AppointmentItem::getId));
        if (items != null && !items.isEmpty()) {
            r.setItems(items.stream().map(it -> {
                AppointmentItemView v = new AppointmentItemView();
                v.setItemName(it.getItemName());
                v.setQuantity(it.getQuantity());
                v.setNote(it.getNote());
                return v;
            }).toList());
        }

        if (!AppointmentStatus.APPROVED.name().equals(appointment.getStatus())
                && !AppointmentStatus.CHECKED_IN.name().equals(appointment.getStatus())) {
            r.setResult("INVALID");
            r.setMessage("预约状态无效");
            return r;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(appointment.getVisitStartTime()) || now.isAfter(appointment.getVisitEndTime())) {
            r.setResult("EXPIRED");
            r.setMessage("不在预约有效时间范围内");
            return r;
        }

        if (checkUsed) {
            Long used = accessRecordMapper.selectCount(new LambdaQueryWrapper<AccessRecord>()
                    .eq(AccessRecord::getAppointmentId, appointment.getId()));
            if (used != null && used > 0) {
                r.setResult("USED");
                r.setMessage("已使用");
                return r;
            }
        }

        r.setResult("VALID");
        r.setMessage("有效");
        return r;
    }

    private String toPngBase64(String content, int width, int height) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (WriterException | java.io.IOException e) {
            throw new BizException(500, "生成二维码图片失败");
        }
    }
}
