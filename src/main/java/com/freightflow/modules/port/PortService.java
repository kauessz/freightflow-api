package com.freightflow.modules.port;

import com.freightflow.modules.port.dto.PortResponse;
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

    public PortResponse getByUnlocode(String unlocode) {
        log.debug("Fetching port unlocode={}", unlocode);
        Port port = portRepository.findByUnlocode(unlocode.toUpperCase())
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
}
