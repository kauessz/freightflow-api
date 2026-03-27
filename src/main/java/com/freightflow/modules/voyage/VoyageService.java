package com.freightflow.modules.voyage;

import com.freightflow.modules.port.Port;
import com.freightflow.modules.port.PortRepository;
import com.freightflow.modules.vessel.Vessel;
import com.freightflow.modules.vessel.VesselRepository;
import com.freightflow.modules.voyage.dto.CreateVoyageRequest;
import com.freightflow.modules.voyage.dto.UpdateVoyageRequest;
import com.freightflow.modules.voyage.dto.VoyageResponse;
import com.freightflow.shared.exception.BusinessException;
import com.freightflow.shared.exception.ResourceNotFoundException;
import com.freightflow.shared.pagination.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class VoyageService {

    private static final Logger log = LoggerFactory.getLogger(VoyageService.class);

    private final VoyageRepository voyageRepository;
    private final VesselRepository vesselRepository;
    private final PortRepository portRepository;

    public VoyageService(VoyageRepository voyageRepository,
                         VesselRepository vesselRepository,
                         PortRepository portRepository) {
        this.voyageRepository = voyageRepository;
        this.vesselRepository = vesselRepository;
        this.portRepository = portRepository;
    }

    // ==================== Queries ====================

    public PageResponse<VoyageResponse> list(Pageable pageable) {
        log.debug("Listing voyages");
        var page = voyageRepository.findAllWithDetails(pageable);
        return PageResponse.from(page.map(VoyageResponse::from));
    }

    public VoyageResponse getById(UUID id) {
        log.debug("Fetching voyage id={}", id);
        Voyage voyage = voyageRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voyage", id));
        return VoyageResponse.from(voyage);
    }

    public VoyageResponse getByVoyageNumber(String voyageNumber) {
        log.debug("Fetching voyage number={}", voyageNumber);
        Voyage voyage = voyageRepository.findByVoyageNumberWithDetails(voyageNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Voyage", voyageNumber));
        return VoyageResponse.from(voyage);
    }

    // ==================== Commands ====================

    @Transactional
    public VoyageResponse create(CreateVoyageRequest request) {
        log.info("Creating voyage number={}", request.voyageNumber());

        if (voyageRepository.existsByVoyageNumber(request.voyageNumber())) {
            throw new BusinessException("Voyage number " + request.voyageNumber() + " already exists");
        }

        if (request.eta().isBefore(request.etd())) {
            throw new BusinessException("ETA must be after ETD");
        }

        Vessel vessel = vesselRepository.findById(request.vesselId())
                .orElseThrow(() -> new ResourceNotFoundException("Vessel", request.vesselId()));

        Port originPort = portRepository.findById(request.originPortId())
                .orElseThrow(() -> new ResourceNotFoundException("Port", request.originPortId()));

        Port destinationPort = portRepository.findById(request.destinationPortId())
                .orElseThrow(() -> new ResourceNotFoundException("Port", request.destinationPortId()));

        if (originPort.getId().equals(destinationPort.getId())) {
            throw new BusinessException("Origin and destination ports must be different");
        }

        Voyage voyage = new Voyage(
                request.voyageNumber(), vessel, originPort, destinationPort,
                request.etd(), request.eta()
        );

        Voyage saved = voyageRepository.save(voyage);
        log.info("Voyage created: id={}, number={}", saved.getId(), saved.getVoyageNumber());
        return VoyageResponse.from(saved);
    }

    @Transactional
    public VoyageResponse update(UUID id, UpdateVoyageRequest request) {
        log.info("Updating voyage id={}", id);
        Voyage voyage = voyageRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voyage", id));

        if (request.status() != null) {
            voyage.setStatus(request.status());
        }
        if (request.etd() != null) {
            voyage.setEtd(request.etd());
        }
        if (request.eta() != null) {
            voyage.setEta(request.eta());
        }
        if (request.atd() != null) {
            voyage.setAtd(request.atd());
        }
        if (request.ata() != null) {
            voyage.setAta(request.ata());
        }

        Voyage saved = voyageRepository.save(voyage);
        return VoyageResponse.from(saved);
    }

    @Transactional
    public void delete(UUID id) {
        log.info("Deleting voyage id={}", id);
        Voyage voyage = voyageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voyage", id));

        if (!voyage.getShipments().isEmpty()) {
            throw new BusinessException("Cannot delete voyage with " + voyage.getShipments().size() + " active shipments");
        }

        voyageRepository.delete(voyage);
    }
}
