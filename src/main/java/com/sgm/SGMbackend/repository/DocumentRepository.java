// DocumentRepository.java
package com.sgm.SGMbackend.repository;
import com.sgm.SGMbackend.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByEntiteTypeAndEntiteId(String entiteType, Long entiteId);
}
