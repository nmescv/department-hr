package com.github.nmescv.departmenthr.department.service;

import com.github.nmescv.departmenthr.department.converter.DocumentVacationConverter;
import com.github.nmescv.departmenthr.department.dictionary.DocumentStatusDict;
import com.github.nmescv.departmenthr.department.dictionary.RoleDict;
import com.github.nmescv.departmenthr.department.dto.DocumentReassignmentDto;
import com.github.nmescv.departmenthr.department.dto.DocumentVacationDto;
import com.github.nmescv.departmenthr.department.entity.DocumentVacation;
import com.github.nmescv.departmenthr.department.entity.Employee;
import com.github.nmescv.departmenthr.department.repository.DocumentVacationRepository;
import com.github.nmescv.departmenthr.department.repository.EmployeeRepository;
import com.github.nmescv.departmenthr.security.entity.Role;
import com.github.nmescv.departmenthr.security.entity.User;
import com.github.nmescv.departmenthr.security.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.github.nmescv.departmenthr.department.dictionary.RoleDict.*;

@Slf4j
@Service
public class DocumentVacationService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DocumentVacationRepository documentVacationRepository;
    private final DocumentVacationConverter documentVacationConverter;

    public DocumentVacationService(EmployeeRepository employeeRepository,
                                   UserRepository userRepository,
                                   DocumentVacationRepository documentVacationRepository,
                                   DocumentVacationConverter documentVacationConverter) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.documentVacationRepository = documentVacationRepository;
        this.documentVacationConverter = documentVacationConverter;
    }

    /**
     * EMPLOYEE - ???????? ??????????????????
     * BOSS - ?????????????????? ?????????? ?????????????? ?????????????????? ???????????? ?????????? ??????????????????????
     * HR - ?????? ??????????????????
     *
     * @param username ?????????????????? ?????????? ????????????????????
     * @return ???????????? ????????????????????
     */
    public List<DocumentVacationDto> findAll(String username) {

        log.info("Vacation Documents - ?????????? ???????? ????????????????????: ?????????? ????????????????????");
        Employee employee = employeeRepository.findByTabelNumber(username);
        if (employee == null) {
            return null;
        }
        log.info("Vacation Documents - ?????????? ???????? ????????????????????: ?????????????????? ????????????");
        log.info("Vacation Documents - ?????????? ???????? ????????????????????: ?????????? ???????????????? ????????????????????");
        User user = userRepository.findByEmployee(employee);
        if (user == null) {
            return null;
        }
        log.info("Vacation Documents - ?????????? ???????? ????????????????????: ?????????????? ???????????????????? ????????????????????");
        log.info("Vacation Documents - ?????????? ???????? ????????????????????: ???????????????????? ???????? ????????????????????");
        for (Role role : user.getRoles()) {
            if (role.getName().equals(HR_ROLE)) {
                log.info("Vacation Documents - ?????????? ???????? ????????????????????: ???????? - HR");
                return documentVacationRepository
                        .findAll()
                        .stream()
                        .map(documentVacationConverter::toDto)
                        .collect(Collectors.toList());
            }
        }

        for (Role role : user.getRoles()) {
            if (role.getName().equals(BOSS_ROLE)) {
                log.info("Vacation Documents - ?????????? ???????? ????????????????????: ???????? - BOSS");
                val list = documentVacationRepository
                        .findAllByBoss(employee)
                        .stream()
                        .map(documentVacationConverter::toDto)
                        .collect(Collectors.toList());
                log.info("Vacation Documents - ?????????? ???????? ????????????????????: ???????? - BOSS: {}", list.toString());
                return list;
            }
        }

        for (Role role : user.getRoles()) {
            if (role.getName().equals(EMPLOYEE_ROLE)) {
                log.info("Vacation Documents - ?????????? ???????? ????????????????????: ???????? - EMPLOYEE");
                val list = documentVacationRepository
                        .findAllByEmployee(employee);
                log.info("Vacation Documents - ?????????? ???????? ????????????????????: ???????? - EMPLOYEE: {}", list.toString());
                return list
                        .stream()
                        .map(documentVacationConverter::toDto)
                        .collect(Collectors.toList());

            }
        }

        log.info("Vacation Documents - ?????????? ???????? ????????????????????: ?????? ?????????????? ???????????????????? ?????? ?????????????? ????????????????????");
        return null;
    }

    /**
     * ROLE: ??????????????????
     * <p>
     * ?????????????????? ?????????????? ???????????? ???? ????????????
     *
     * @return ???????????????? ?? ???????????????????? ???? ????????????, ???????????? "????????????"
     */
    @Transactional
    public DocumentVacationDto createRequestForVacation(DocumentVacationDto dto, String username) {

        String orderNumber = UUID.randomUUID().toString();
        if (orderNumber.length() > 30) {
            orderNumber = orderNumber.substring(0, 30);
        }
        dto.setOrderNumber(orderNumber);
        dto.setDocumentStatus(DocumentStatusDict.OPEN.getStatus());
        dto.setEmployeeId(employeeRepository.findByTabelNumber(username).getId());
        dto.setCreatedAt(new Date());

        DocumentVacation entity = documentVacationConverter.toEntity(dto);
        DocumentVacation saved = documentVacationRepository.save(entity);
        return documentVacationConverter.toDto(saved);
    }


    /**
     * ?????????? ?????????????????? ???? ???????????????????????????? ??????????????????
     * EMPLOYEE - ?????????? ???????????? ???????? ??????????????????
     * BOSS - ?????????? ???????? ?????????????????? + ?????????????????? ?????????? ??????????????????????
     * HR - ?????????? ?????? ??????????????????
     *
     * @param documentId ?????????????????????????? ??????????????????
     * @param username   ?????????????????? ?????????? ????????????????????????
     * @return ???????????????????? ?? ??????????????????
     */
    public DocumentVacationDto showById(Long documentId, String username) {

        DocumentVacation documentVacation = documentVacationRepository.findById(documentId).orElse(null);
        if (documentVacation == null) {
            return null;
        }

        log.info("Vacation Documents - ?????????? ??????????????????: ?????????? ????????????????????");
        Employee employee = employeeRepository.findByTabelNumber(username);
        if (employee == null) {
            return null;
        }

        log.info("Vacation Documents - ?????????? ??????????????????: ?????????????????? ????????????");
        log.info("Vacation Documents - ?????????? ??????????????????: ?????????? ???????????????? ????????????????????");
        User user = userRepository.findByEmployee(employee);
        if (user == null) {
            return null;
        }
        log.info("Vacation Documents - ?????????? ??????????????????: ?????????????? ???????????????????? ????????????????????");
        log.info("Vacation Documents - ?????????? ??????????????????: ???????????????????? ???????? ????????????????????");

        for (Role role : user.getRoles()) {
            if (role.getName().equals(HR_ROLE)) {
                log.info("Vacation Documents - ?????????? ??????????????????: ???????? - HR");
                log.info(documentVacation.getDocumentStatus().getName());
                return documentVacationConverter.toDto(documentVacation);
            }

            if (role.getName().equals(BOSS_ROLE)) {
                log.info("Vacation Documents - ?????????? ??????????????????: ???????? - BOSS");
                if (documentVacation.getBoss().getTabelNumber().equals(username)) {
                    log.info(documentVacation.getDocumentStatus().getName());
                    return documentVacationConverter.toDto(documentVacation);
                }
            }

            if (role.getName().equals(EMPLOYEE_ROLE)) {
                log.info("Vacation Documents - ?????????? ??????????????????: ???????? - EMPLOYEE");
                if (documentVacation.getEmployee().getTabelNumber().equals(username)) {
                    log.info(documentVacation.getDocumentStatus().getName());
                    return documentVacationConverter.toDto(documentVacation);
                }
            }
        }

        return null;
    }

    /**
     * ROLE: ??????????????????
     * <p>
     * ?????????????????? ???????????????????????? ???????????? ????????????????????
     *
     * @return ?????????????????????????? ????????????????, ???????????? "?? ????????????????"
     */
    public DocumentVacationDto approveVacation(Long id, String username) {
        DocumentVacationDto dto = showById(id, username);
        dto.setIsApproved(Boolean.TRUE);
        dto.setDocumentStatus(DocumentStatusDict.IN_PROCESS.getStatus());
        DocumentVacation entity = documentVacationConverter.toEntity(dto);
        DocumentVacation saved = documentVacationRepository.save(entity);
        return documentVacationConverter.toDto(saved);
    }

    /**
     * ROLE: ??????????????????
     *
     * @return ?????????????????????? ????????????????, ???????????? "?? ????????????????"
     */
    public DocumentVacationDto declineVacation(Long id, String username) {
        DocumentVacationDto dto = showById(id, username);
        dto.setIsApproved(Boolean.FALSE);
        dto.setDocumentStatus(DocumentStatusDict.IN_PROCESS.getStatus());
        DocumentVacation entity = documentVacationConverter.toEntity(dto);
        DocumentVacation saved = documentVacationRepository.save(entity);
        return documentVacationConverter.toDto(saved);
    }

    /**
     * ROLE: HR
     * <p>
     * ?????????????????? ???????????????????? ??????????????????
     *
     * @return ?????????????????????? ????????????????, ???????????? "????????????"
     */
    public DocumentVacationDto closeDocument(Long id, String username) {
        DocumentVacationDto dto = showById(id, username);
        dto.setHr(employeeRepository.findByTabelNumber(username).getId());
        dto.setDocumentStatus(DocumentStatusDict.CLOSED.getStatus());
        DocumentVacation entity = documentVacationConverter.toEntity(dto);
        DocumentVacation saved = documentVacationRepository.save(entity);
        return documentVacationConverter.toDto(saved);
    }
}
