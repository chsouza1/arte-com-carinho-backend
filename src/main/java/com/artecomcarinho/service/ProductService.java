package com.artecomcarinho.service;

import com.artecomcarinho.dto.ProductDTO;
import com.artecomcarinho.model.Product;
import com.artecomcarinho.model.Product.ProductCategory;
import com.artecomcarinho.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable)
                .map(this::convertToDTO);
    }

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(p -> modelMapper.map(p, ProductDTO.class))
                .toList();
    }


    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + id));
        return convertToDTO(product);
    }

    public Page<ProductDTO> getProductsByCategory(ProductCategory category, Pageable pageable) {
        return productRepository.findByCategoryAndActiveTrue(category, pageable)
                .map(this::convertToDTO);
    }

    public List<ProductDTO> getFeaturedProducts() {
        return productRepository.findByFeaturedTrueAndActiveTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Page<ProductDTO> searchProducts(String search, Pageable pageable) {
        return productRepository.searchProducts(search, pageable)
                .map(this::convertToDTO);
    }

    public List<ProductDTO> getLowStockProducts(Integer threshold) {
        return productRepository.findLowStockProducts(threshold)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        // Validar SKU único
        if (productDTO.getSku() != null && productRepository.findBySku(productDTO.getSku()).isPresent()) {
            throw new RuntimeException("SKU já existe: " + productDTO.getSku());
        }

        Product product = convertToEntity(productDTO);
        product.setActive(true);

        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + id));

        // Validar SKU único (se mudou)
        if (productDTO.getSku() != null && !productDTO.getSku().equals(existingProduct.getSku())) {
            if (productRepository.findBySku(productDTO.getSku()).isPresent()) {
                throw new RuntimeException("SKU já existe: " + productDTO.getSku());
            }
        }

        // Atualizar campos
        existingProduct.setName(productDTO.getName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setCategory(productDTO.getCategory());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setStock(productDTO.getStock());
        existingProduct.setSku(productDTO.getSku());
        existingProduct.setImages(productDTO.getImages());
        existingProduct.setSizes(productDTO.getSizes());
        existingProduct.setColors(productDTO.getColors());
        existingProduct.setFeatured(productDTO.getFeatured());
        existingProduct.setCustomizable(productDTO.getCustomizable());

        Product updatedProduct = productRepository.save(existingProduct);
        return convertToDTO(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + id));

        // Soft delete
        product.setActive(false);
        productRepository.save(product);
    }

    @Transactional
    public void updateStock(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + id));

        int newStock = product.getStock() + quantity;
        if (newStock < 0) {
            throw new RuntimeException("Estoque insuficiente");
        }

        product.setStock(newStock);
        productRepository.save(product);
    }

    @Transactional
    public void toggleFeatured(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + id));

        boolean currentStatus = Boolean.TRUE.equals(product.getFeatured());
        product.setFeatured(!currentStatus);

        productRepository.save(product);
    }

    private ProductDTO convertToDTO(Product product) {
        if (product == null) return null;

        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .category(product.getCategory())
                .sku(product.getSku())
                .active(product.getActive())
                .featured(product.getFeatured())
                .customizable(product.getCustomizable())
                .images(product.getImages())
                .sizes(product.getSizes())
                .colors(product.getColors())
                .build();
    }

    private Product convertToEntity(ProductDTO productDTO) {
        return modelMapper.map(productDTO, Product.class);
    }
}