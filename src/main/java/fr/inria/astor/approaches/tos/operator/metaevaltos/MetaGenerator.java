package fr.inria.astor.approaches.tos.operator.metaevaltos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import fr.inria.astor.approaches.cardumen.FineGrainedExpressionReplaceOperator;
import fr.inria.astor.approaches.tos.core.InsertMethodOperator;
import fr.inria.astor.core.entities.Ingredient;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.StatementOperatorInstance;
import fr.inria.astor.core.entities.meta.MetaOperatorInstance;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.solutionsearch.spaces.operators.AstorOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtTry;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.code.CtBlockImpl;
import spoon.support.reflect.code.CtReturnImpl;

/**
 * 
 * @author Matias Martinez
 *
 */
public class MetaGenerator {

	protected static Logger log = Logger.getLogger(Thread.currentThread().getName());

	public static MetaOperatorInstance createMetaFineGrainedReplacement(ModificationPoint modificationPoint,
			CtExpression elementSource, int variableCounter, List<Ingredient> ingredients,
			List<CtParameter<?>> parameters, List<CtExpression<?>> realParameters, AstorOperator parentOperator,
			CtTypeReference returnType) {

		List<OperatorInstance> opsOfVariant = new ArrayList();
		Map<Integer, Ingredient> ingredientOfMapped = new HashMap<>();

		createMetaForSingleElement(modificationPoint, elementSource, variableCounter, ingredients, parameters,
				realParameters, returnType, opsOfVariant, ingredientOfMapped);

		MetaOperatorInstance opMega = new MetaOperatorInstance(opsOfVariant);
		opMega.setAllIngredients(ingredientOfMapped);
		opMega.setOperationApplied(parentOperator);
		opMega.setOriginal(modificationPoint.getCodeElement());
		opMega.setModificationPoint(modificationPoint);

		return opMega;
	}

	public static void createMetaForSingleElement(ModificationPoint modificationPoint, CtExpression elementSource,
			int variableCounter, List<Ingredient> ingredients, List<CtParameter<?>> parameters,
			List<CtExpression<?>> realParameters, CtTypeReference returnType, List<OperatorInstance> opsOfVariant,
			Map<Integer, Ingredient> ingredientOfMapped) {
		CtExpression defaultReturnElement = elementSource;

		// Creation of mega method
		CtMethod<?> megaMethod = createMegaMethod(modificationPoint, defaultReturnElement, variableCounter, ingredients,
				parameters, ingredientOfMapped, returnType);

		// Invocation to mega
		CtInvocation newInvocationToMega = creationInvocationToMega(modificationPoint, realParameters, megaMethod);

		// Now the if to be inserted:
		// 1:

		// Let's create the operations

		OperatorInstance opInvocation = new OperatorInstance();
		opInvocation.setOperationApplied(new FineGrainedExpressionReplaceOperator());
		opInvocation.setOriginal(elementSource);
		opInvocation.setModified(newInvocationToMega);
		opInvocation.setModificationPoint(modificationPoint);

		opsOfVariant.add(opInvocation);

		// 2:
		// The meta method to be added
		OperatorInstance opMethodAdd = new OperatorInstance();
		opMethodAdd.setOperationApplied(new InsertMethodOperator());
		opMethodAdd.setOriginal(modificationPoint.getCodeElement());
		opMethodAdd.setModified(megaMethod);
		opMethodAdd.setModificationPoint(modificationPoint);
		opsOfVariant.add(opMethodAdd);

		//
		log.debug("method: \n" + megaMethod);

		log.debug("invocation: \n" + newInvocationToMega);
	}

	public static MetaOperatorInstance createMetaStatementReplacement(ModificationPoint modificationPoint,
			CtElement elementSource, CtExpression defaultReturnElement, int variableCounter,
			List<Ingredient> ingredients, List<CtParameter<?>> parameters, List<CtExpression<?>> realParameters,
			AstorOperator parentOperator, CtTypeReference returnType) {
		List<OperatorInstance> opsOfVariant = new ArrayList();
		Map<Integer, Ingredient> ingredientOfMapped = new HashMap<>();

		// Creation of mega method
		CtMethod<?> megaMethod = createMegaMethod(modificationPoint, defaultReturnElement, variableCounter, ingredients,
				parameters, ingredientOfMapped, returnType);

		CtInvocation newInvocationToMega = creationInvocationToMega(modificationPoint, realParameters, megaMethod);

		// Now the if to be inserted:

		CtIf ifNew = MutationSupporter.getFactory().createIf();

		CtStatement statementPointed = (CtStatement) modificationPoint.getCodeElement();
		CtStatement statementPointedCloned = statementPointed.clone();
		statementPointedCloned.setPosition(new NoSourcePosition());
		MutationSupporter.clearPosition(statementPointedCloned);

		ifNew.setThenStatement(statementPointedCloned);
		ifNew.setCondition(newInvocationToMega);

		// Let's create the operations
		OperatorInstance opInstace = new StatementOperatorInstance(modificationPoint, parentOperator, statementPointed,
				ifNew);
		opsOfVariant.add(opInstace);

		// The meta method to be added
		OperatorInstance opMethodAdd = new OperatorInstance();
		opMethodAdd.setOperationApplied(new InsertMethodOperator());
		opMethodAdd.setOriginal(modificationPoint.getCodeElement());
		opMethodAdd.setModified(megaMethod);
		opMethodAdd.setModificationPoint(modificationPoint);
		opsOfVariant.add(opMethodAdd);

		//
		log.debug("method: \n" + megaMethod);

		log.debug("invocation: \n" + newInvocationToMega);

		MetaOperatorInstance opMega = new MetaOperatorInstance(opsOfVariant);
		opMega.setAllIngredients(ingredientOfMapped);
		opMega.setOperationApplied(parentOperator);
		opMega.setOriginal(modificationPoint.getCodeElement());
		opMega.setModificationPoint(modificationPoint);

		return opMega;
	}

	public static CtInvocation creationInvocationToMega(ModificationPoint modificationPoint,
			List<CtExpression<?>> realParameters, CtMethod<?> megaMethod) {
		CtType target = modificationPoint.getCodeElement().getParent(CtType.class);
		CtExpression invocationTarget = MutationSupporter.getFactory().createThisAccess(target.getReference());

		// Here, we have to consider if the parent method is static.
		CtMethod parentMethod = modificationPoint.getCodeElement().getParent(CtMethod.class);
		if (parentMethod != null) {

			if (parentMethod.getModifiers().contains(ModifierKind.STATIC)) {
				// modifiers.add(ModifierKind.STATIC);
				invocationTarget = MutationSupporter.getFactory().createTypeAccess(target.getReference());

			}
		}

		// Invocation to mega

		CtInvocation newInvocationToMega = MutationSupporter.getFactory().createInvocation(invocationTarget,
				megaMethod.getReference(), realParameters);
		return newInvocationToMega;
	}

	public static CtMethod<?> createMegaMethod(ModificationPoint modificationPoint, CtExpression defaultReturnElement,
			int variableCounter, List<Ingredient> ingredients, List<CtParameter<?>> parameters,
			Map<Integer, Ingredient> ingredientOfMapped, CtTypeReference returnType) {

		String name = "_meta_" + variableCounter;
		CtType<?> target = modificationPoint.getCodeElement().getParent(CtType.class);
		Set<ModifierKind> modifiers = new HashSet<>();
		modifiers.add(ModifierKind.PRIVATE);

		Set<CtTypeReference<? extends Throwable>> thrownTypes = new HashSet<>();

		// Here, we have to consider if the parent method is static.
		CtMethod parentMethod = modificationPoint.getCodeElement().getParent(CtMethod.class);
		if (parentMethod != null) {

			if (parentMethod.getModifiers().contains(ModifierKind.STATIC)) {
				modifiers.add(ModifierKind.STATIC);

			}
			// We add the throws declared by the method

			for (Object obT : parentMethod.getThrownTypes()) {
				CtTypeReference<? extends Throwable> typeEx = ((CtTypeReference<? extends Throwable>) obT).clone();
				typeEx.setPosition(new NoSourcePosition());
				thrownTypes.add(typeEx);

			}
		}

		parameters.stream().forEach(e -> e.setPositions(new NoSourcePosition()));

		CtMethod<?> megaMethod = MutationSupporter.getFactory().createMethod(target, modifiers, returnType, name,
				parameters, thrownTypes);

		/// Let's start creating the body of the new method.
		// first the main try
		CtTry tryMethodMain = MutationSupporter.getFactory().createTry();
		List<CtCatch> catchers = new ArrayList<>();
		CtCatch catch1 = MutationSupporter.getFactory().createCtCatch("e", Exception.class, new CtBlockImpl());
		catchers.add(catch1);
		tryMethodMain.setCatchers(catchers);
		CtBlock tryBoddy = new CtBlockImpl();
		tryMethodMain.setBody(tryBoddy);

		CtBlock methodBodyBlock = new CtBlockImpl();
		megaMethod.setBody(methodBodyBlock);
		methodBodyBlock.addStatement(tryMethodMain);
		// Let's start the counter according to the number of operation mutants we
		// already have
		int candidateNumber = ingredientOfMapped.keySet().size();
		for (Ingredient ingredientCandidate : ingredients) {

			candidateNumber++;
			CtExpression expressionCandidate = (CtExpression) ingredientCandidate.getCode();
			CtExpression expCloned = expressionCandidate.clone();
			expCloned.setPosition(new NoSourcePosition());
			MutationSupporter.clearPosition(expCloned);
			CtCodeSnippetExpression caseCondition = MutationSupporter.getFactory().createCodeSnippetExpression(
					"\"" + candidateNumber + "\".equals(System.getProperty(\"mutnumber\")) ");

			ingredientOfMapped.put(candidateNumber, ingredientCandidate);

			CtIf particularIf = MutationSupporter.getFactory().createIf();
			particularIf.setCondition(caseCondition);
			CtBlock particularIfBlock = new CtBlockImpl<>();

			if (ConfigurationProperties.getPropertyBool("meta_add_syso")) {

				CtStatement stPrint = MutationSupporter.getFactory().createCodeSnippetStatement(
						"System.out.println(" + "\"\\nPROPERTY met:\" +System.getProperty(\"mutnumber\"))");
				particularIfBlock.addStatement(stPrint);
			}
			particularIf.setThenStatement(particularIfBlock);

			// The return inside the if
			// add a return with the expression
			CtReturn casereturn = new CtReturnImpl<>();
			casereturn.setReturnedExpression(expCloned);
			particularIfBlock.addStatement(casereturn);

			// Add the if to the methodBlock
			// methodBodyBlock
			tryBoddy.addStatement(particularIf);

		}

		// By default, return the original
		CtReturn defaultReturnLast = new CtReturnImpl<>();
		CtExpression expCloned = defaultReturnElement.clone();
		expCloned.setPosition(new NoSourcePosition());
		MutationSupporter.clearPosition(expCloned);

		defaultReturnLast.setReturnedExpression(expCloned);
		methodBodyBlock.addStatement(defaultReturnLast);
		return megaMethod;
	}
}
