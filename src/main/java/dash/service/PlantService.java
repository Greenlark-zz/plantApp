package dash.service;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;

import dash.dao.PlantEntity;
import dash.errorhandling.AppException;
import dash.pojo.Group;
import dash.pojo.Plant;
import dash.pojo.Post;


public interface PlantService
{

	/*
	 * ******************* Read related methods ********************
	 */
	
	public Plant getPlantByCommonName(String commonName) throws AppException;
	
	public List<Plant> getPlantsAlpha(Integer nativeField, Integer colorTimin,
			Integer soilConditions, Integer sunAmount, Integer growthSize)
		throws AppException;

	public Long createPlant(Plant comment, Group group);

	public void createPlants(List<Plant> comments);

	public List<Plant> getPlantsByPost(int numberOfPlants, Long startIndex,
			Post post);

	public void updateFullyPlant(Plant commentById);

	public void deletePlant(Plant comment);
}
