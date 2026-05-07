package com.ISI.Appel_classe.Gestion_des_justificatifs.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateJustificatifRequest {

    private Long presenceId;
    private String motif;
    private String commentaire;
    private MultipartFile document; // PDF, image scannée, certificat médical, attestation
}
