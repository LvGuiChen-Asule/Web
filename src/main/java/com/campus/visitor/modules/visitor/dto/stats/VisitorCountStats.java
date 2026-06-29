package com.campus.visitor.modules.visitor.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisitorCountStats {
    private String date;
    private Integer count;
}
