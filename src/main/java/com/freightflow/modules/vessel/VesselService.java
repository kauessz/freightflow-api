package com.freightflow.modules.vessel;

import com.freightflow.modules.vessel.dto.CreateVesselRequest;
import com.freightflow.modules.vessel.dto.UpdateVesselRequest;
import com.freightflow.modules.vessel.dto.VesselResponse;
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
public class VesselService {

    private static final Logger log = LoggerFactory.getLogger(VesselService.class);

    private final VesselRepository vesselRepository;

    public VesselService(VesselRepository vesselRepository) {
        this.vesselRepository = vesselRepository;
    }

    public PageResponse<VesselResponse> list(Pageable pageable) {
        log.debug("Listing vessels");
        var page = vesselRepository.findAll(pageable);
        return PageResponse.from(page.map(VesselResponse::from));
    }

    public VesselResponse getById(UUID id) {
        log.debug("Fetching vessel id={}", id);
        Vessel vessel = vesselRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vessel", id));
        return VesselResponse.from(vessel);
    }

    public VesselResponse getByImo(String imo) {
        log.debug("Fetching vessel imo={}", imo);
        Vessel vessel = vesselRepository.findByImo(imo)
                .orElseThrow(() -> new ResourceNotFoundException("Vessel", imo));
        return VesselResponse.from(vessel);
    }

    @Transactional
    public VesselResponse create(CreateVesselRequest request) {
        log.info("Creating vessel imo={}", request.imo());

        if (vesselRepository.existsByImo(request.imo())) {
            throw new BusinessException("Vessel with IMO " + request.imo() + " already exists");
        }

        Vessel vessel = new Vessel(
                request.imo(), request.name(), request.flag(),
                request.type(), request.capacityTeu()
        );

        Vessel saved = vesselRepository.save(vessel);
        log.info("Vessel created: id={}, imo={}, name={}", saved.getId(), saved.getImo(), saved.getName());
        return VesselResponse.from(saved);
    }

    @Transactional
    public VesselResponse update(UUID id, UpdateVesselRequest request) {
        log.info("Updating vessel id={}", id);
        Vessel vessel = vesselRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vessel", id));

        if (request.name() != null) vessel.setName(request.name());
        if (request.flag() != null) vessel.setFlag(request.flag());
        if (request.type() != null) vessel.setType(request.type());
        if (request.capacityTeu() != null) vessel.setCapacityTeu(request.capacityTeu());

        Vessel saved = vesselRepository.save(vessel);
        return VesselResponse.from(saved);
    }

    @Transactional
    public void delete(UUID id) {
        log.info("Deleting vessel id={}", id);
        Vessel vessel = vesselRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vessel", id));

        if (!vessel.getVoyages().isEmpty()) {
            throw new BusinessException("Cannot delete vessel with " + vessel.getVoyages().size() + " voyages");
        }

        vesselRepository.delete(vessel);
    }
}
