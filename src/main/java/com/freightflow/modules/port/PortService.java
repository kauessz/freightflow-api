package com.freightflow.modules.port;

import com.freightflow.modules.port.dto.CreatePortRequest;
import com.freightflow.modules.port.dto.PortResponse;
import com.freightflow.modules.port.dto.UpdatePortRequest;
import com.freightflow.shared.exception.BusinessException;
import com.freightflow.shared.exception.ResourceNotFoundException;
import com.freightflow.shared.pagination.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PortService {

    private static final Logger log = LoggerFactory.getLogger(PortService.class);

    private final PortRepository portRepository;

    public PortService(PortRepository portRepository) {
        this.portRepository = portRepository;
    }

    public PageResponse<PortResponse> list(Pageable pageable) {
        log.debug("Listing ports");
        var page = portRepository.findAll(pageable);
        return PageResponse.from(page.map(PortResponse::from));
    }

    public List<PortResponse> listByCountry(String country) {
        log.debug("Listing ports for country={}", country);
        return portRepository.findByCountryOrderByName(country)
                .stream()
                .map(PortResponse::from)
                .toList();
    }

    public PortResponse getById(UUID id) {
        log.debug("Fetching port id={}", id);
        Port port = portRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Port", id));
        return PortResponse.from(port);
    }

    /** UNLOCODE format: 2 uppercase country letters + 3 uppercase location chars (letters or digits). */
    private static final java.util.regex.Pattern UNLOCODE_PATTERN =
            java.util.regex.Pattern.compile("^[A-Z]{2}[A-Z0-9]{3}$");

    public PortResponse getByUnlocode(String unlocode) {
        log.debug("Fetching port unlocode={}", unlocode);
        String upper = unlocode.toUpperCase();
        if (!UNLOCODE_PATTERN.matcher(upper).matches()) {
            throw new BusinessException(
                    "Invalid UNLOCODE format: '" + unlocode + "'. Expected 2 country letters + 3 location chars (e.g., BRSSZ).");
        }
        Port port = portRepository.findByUnlocode(upper)
                .orElseThrow(() -> new ResourceNotFoundException("Port", unlocode));
        return PortResponse.from(port);
    }

    public List<PortResponse> search(String query) {
        log.debug("Searching ports: {}", query);
        return portRepository.searchByNameOrUnlocode(query)
                .stream()
                .map(PortResponse::from)
                .toList();
    }

    @Transactional
    public PortResponse create(CreatePortRequest request) {
        String unlocode = request.unlocode().toUpperCase();
        if (portRepository.existsByUnlocode(unlocode)) {
            throw new BusinessException("Port with UNLOCODE " + unlocode + " already exists");
        }

        Port port = new Port(
                unlocode,
                request.name(),
                request.country().toUpperCase(),
                request.timezone(),
                request.latitude(),
                request.longitude()
        );
        if (request.active() != null) {
            port.setActive(request.active());
        }

        return PortResponse.from(portRepository.save(port));
    }

    @Transactional
    public PortResponse update(UUID id, UpdatePortRequest request) {
        Port port = portRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Port", id));

        if (request.unlocode() != null) {
            String unlocode = request.unlocode().toUpperCase();
            if (portRepository.existsByUnlocodeAndIdNot(unlocode, id)) {
                throw new BusinessException("Port with UNLOCODE " + unlocode + " already exists");
            }
            port.setUnlocode(unlocode);
        }
        if (request.name() != null) port.setName(request.name());
        if (request.country() != null) port.setCountry(request.country().toUpperCase());
        if (request.timezone() != null) port.setTimezone(request.timezone());
        if (request.latitude() != null || request.longitude() != null) {
            port.setLatitude(request.latitude());
            port.setLongitude(request.longitude());
        }
        if (request.active() != null) port.setActive(request.active());

        return PortResponse.from(portRepository.save(port));
    }
}
