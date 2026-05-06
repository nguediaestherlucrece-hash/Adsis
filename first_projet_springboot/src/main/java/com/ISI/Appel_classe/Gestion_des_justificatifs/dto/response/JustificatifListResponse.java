package com.ISI.Appel_classe.Gestion_des_justificatifs.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JustificatifListResponse {

    private List<JustificatifResponse> content;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
}
