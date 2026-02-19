package com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.AssistantPedagogiqueCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.AssistantPedagogiqueUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.AssistantPedagogiqueDTO;

import java.util.List;

public interface AssistantPedagogiqueService {

    List<AssistantPedagogiqueDTO> getAllAssistants();

    AssistantPedagogiqueDTO getAssistantById(Long id);

    AssistantPedagogiqueDTO getAssistantByEmail(String email);

    List<AssistantPedagogiqueDTO> getAssistantsByDepartement(String departement);

    AssistantPedagogiqueDTO createAssistant(AssistantPedagogiqueCreateDTO dto);

    AssistantPedagogiqueDTO updateAssistant(Long id, AssistantPedagogiqueUpdateDTO dto);

    void deleteAssistant(Long id);
}
