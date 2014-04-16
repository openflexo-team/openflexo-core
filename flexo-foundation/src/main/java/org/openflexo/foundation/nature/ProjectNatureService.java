package org.openflexo.foundation.nature;

import java.util.List;

import org.openflexo.foundation.FlexoService;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;

/**
 * This service provides management layer for {@link ProjectNature}<br>
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(DefaultProjectNatureService.class)
public interface ProjectNatureService extends FlexoService {
	public static final String PROJECT_NATURES = "projectNatures";

	@Getter(value = PROJECT_NATURES, cardinality = Cardinality.LIST, ignoreType = true)
	public List<ProjectNature<?, ?>> getProjectNatures();

	@Setter(PROJECT_NATURES)
	public void setProjectNatures(List<ProjectNature<?, ?>> projectNatures);

	@Adder(PROJECT_NATURES)
	public void addToProjectNatures(ProjectNature<?, ?> projectNature);

	@Remover(PROJECT_NATURES)
	public void removeFromProjectNatures(ProjectNature<?, ?> projectNature);

	/**
	 * Return project nature mapping supplied class<br>
	 * 
	 * @param projectNatureClass
	 * @return
	 */
	public <N extends ProjectNature<?, ?>> N getProjectNature(Class<N> projectNatureClass);

	/**
	 * Return project nature mapping supplied class<br>
	 * 
	 * @param projectNatureClass
	 * @return
	 */
	public ProjectNature<?, ?> getProjectNature(String projectNatureClassName);

}
