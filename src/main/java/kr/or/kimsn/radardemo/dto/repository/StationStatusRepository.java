package kr.or.kimsn.radardemo.dto.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.or.kimsn.radardemo.dto.StationStatusDto;

public interface StationStatusRepository extends JpaRepository<StationStatusDto, String>{

    StationStatusDto findBySiteCd(String sitecd);
}
