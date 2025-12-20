package com.artecomcarinho.service;

import com.artecomcarinho.dto.AddressDTO;
import com.artecomcarinho.exception.ResourceNotFoundException;
import com.artecomcarinho.exception.UnauthorizedException;
import com.artecomcarinho.model.Address;
import com.artecomcarinho.model.User;
import com.artecomcarinho.repository.AddressRepository;
import com.artecomcarinho.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public List<AddressDTO> getUserAddresses(String email) {
        User user = getUserByEmail(email);
        return addressRepository.findByUserId(user.getId())
                .stream()
                .map(AddressDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressDTO createAddress(String email, AddressDTO dto) {
        User user = getUserByEmail(email);

        Address address = Address.builder()
                .user(user)
                .zipCode(dto.getZipCode().replaceAll("\\D", ""))
                .street(dto.getStreet())
                .number(dto.getNumber())
                .complement(dto.getComplement())
                .neighborhood(dto.getNeighborhood())
                .city(dto.getCity())
                .state(dto.getState().toUpperCase())
                .isDefault(dto.isDefault())
                .build();

        if (address.isDefault() || addressRepository.findByUserId(user.getId()).isEmpty()) {
            address.setDefault(true);
            unsetOtherDefaults(user.getId());
        }

        return new AddressDTO(addressRepository.save(address));
    }

    @Transactional
    public AddressDTO updateAddress(String email, Long id, AddressDTO dto) {
        User user = getUserByEmail(email);
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Este endereço não pertence a você");
        }

        address.setZipCode(dto.getZipCode().replaceAll("\\D", ""));
        address.setStreet(dto.getStreet());
        address.setNumber(dto.getNumber());
        address.setComplement(dto.getComplement());
        address.setNeighborhood(dto.getNeighborhood());
        address.setCity(dto.getCity());
        address.setState(dto.getState().toUpperCase());

        if (dto.isDefault() && !address.isDefault()) {
            unsetOtherDefaults(user.getId());
            address.setDefault(true);
        }

        return new AddressDTO(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(String email, Long id) {
        User user = getUserByEmail(email);
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Este endereço não pertence a você");
        }

        addressRepository.delete(address);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    private void unsetOtherDefaults(Long userId) {
        List<Address> addresses = addressRepository.findByUserId(userId);
        for (Address addr : addresses) {
            if (addr.isDefault()) {
                addr.setDefault(false);
                addressRepository.save(addr);
            }
        }
    }
}