package org.openflexo.foundation.test.fml;

import static org.junit.Assert.fail;

import java.util.Iterator;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.DefaultFlexoServiceManager;
import org.openflexo.foundation.FlexoEditingContext;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.localization.LocalizationService;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.test.OpenflexoTestCase.FlexoTestEditor;
import org.openflexo.foundation.test.OpenflexoTestCase.TestProjectLoader;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.model.ModelProperty;

public class CheckDataBindingProperties {

	public static void main(String[] args) {

		FlexoServiceManager serviceManager = new DefaultFlexoServiceManager(null, false, true) {

			@Override
			protected LocalizationService createLocalizationService(String relativePath) {
				LocalizationService returned = super.createLocalizationService(relativePath);
				returned.setAutomaticSaving(false);
				return returned;
			}

			@Override
			protected FlexoEditingContext createEditingContext() {
				// In unit tests, we do NOT want to be warned against unexpected
				// edits
				return FlexoEditingContext.createInstance(false);
			}

			@Override
			protected FlexoTestEditor createApplicationEditor() {
				return new FlexoTestEditor(null, this);
			}

			@Override
			protected TestProjectLoader createProjectLoaderService() {
				return new TestProjectLoader();
			}

		};

		serviceManager.getLocalizationService().setAutomaticSaving(false);

		// Activate both FML and FML@RT technology adapters
		TechnologyAdapterService taService = serviceManager.getTechnologyAdapterService();
		taService.activateTechnologyAdapter(taService.getTechnologyAdapter(FMLTechnologyAdapter.class), true);
		taService.activateTechnologyAdapter(taService.getTechnologyAdapter(FMLRTTechnologyAdapter.class), true);

		try {
			System.out.println("CheckDataBindingProperties");
			// TechnologyAdapterService taService = DefaultTechnologyAdapterService.getNewInstance(null);
			// taService.addToTechnologyAdapters(ta);
			FMLModelFactory factory = new FMLModelFactory(null, serviceManager);
			/*for (Class<?> modelSlotClass : ta.getAvailableModelSlotTypes()) {
				System.out.println("Check: " + modelSlotClass);
				assertNotNull(factory.getModelContext().getModelEntity(modelSlotClass));
			}*/

			PamelaMetaModel pamelaMetaModel = factory.getModelContext();
			int i = 0;
			for (Iterator<ModelEntity> it = pamelaMetaModel.getEntities(); it.hasNext();) {
				ModelEntity<?> e = it.next();
				System.out.println(" > " + i + " : " + e);
				boolean hasProperties = false;
				StringBuffer sb = new StringBuffer();
				sb.append("\t@Override\n");
				sb.append("\tpublic void revalidateBindings() {\n");
				sb.append("\t\tsuper.revalidateBindings();\n");
				for (ModelProperty p : e.getDeclaredProperties()) {
					if (p.getType().equals(DataBinding.class)) {
						System.out.println("     >>> " + p + " type=" + p.getType() + " " + p.getGetterMethod().getName());
						hasProperties = true;
						sb.append("\t\t" + p.getGetterMethod().getName() + "().rebuild();\n");
					}
				}
				sb.append("\t}\n");
				if (hasProperties) {
					System.err.println(sb.toString());
				}
				i++;
				/*try {
					e.checkMethodImplementations(this);
				} catch (MissingImplementationException ex) {
					System.err.println("MissingImplementationException: " + ex.getMessage());
					thrown = ex;
				}*/
			}
			// factory.checkMethodImplementations();

		} catch (ModelDefinitionException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

	/*@Override
	public void revalidateBindings() {
		super.revalidateBindings();
		getExpression().rebuild();
	}*/

}
