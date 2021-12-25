/**
 * 
 */
package com.asl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.asl.model.Trip;

/**
 * @author zubayer
 *
 */
@Repository
public interface TripRepository extends JpaRepository<Trip, String> {

}
