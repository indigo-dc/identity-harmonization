package edu.kit.scc;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface EncryptedAssertionRepository extends CrudRepository<EncryptedAssertion, Long> {

	List<EncryptedAssertion> findByBase64Assertion(String base64Assertion);
}
