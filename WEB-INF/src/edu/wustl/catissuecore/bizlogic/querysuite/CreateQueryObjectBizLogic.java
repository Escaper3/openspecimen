
package edu.wustl.catissuecore.bizlogic.querysuite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import edu.common.dynamicextensions.domaininterface.AttributeInterface;
import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
import edu.wustl.catissuecore.applet.AppletConstants;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.catissuecore.util.global.Utility;
import edu.wustl.common.querysuite.queryobject.ICondition;
import edu.wustl.common.querysuite.queryobject.IConstraints;
import edu.wustl.common.querysuite.queryobject.IExpression;
import edu.wustl.common.querysuite.queryobject.IExpressionId;
import edu.wustl.common.querysuite.queryobject.IExpressionOperand;
import edu.wustl.common.querysuite.queryobject.IParameterizedCondition;
import edu.wustl.common.querysuite.queryobject.IRule;
import edu.wustl.common.querysuite.queryobject.RelationalOperator;
import edu.wustl.common.querysuite.queryobject.impl.ParameterizedCondition;
import edu.wustl.common.util.global.ApplicationProperties;
import edu.wustl.common.util.global.Validator;
import edu.wustl.common.util.logger.Logger;
import edu.wustl.catissuecore.util.querysuite.QueryModuleConstants;

/**
 * Creates Query Object as per the data filled by the user on AddLimits section.
 * This will also validate the inputs and generate messages and they will be
 * shown to user.
 * 
 * @author deepti_shelar
 * 
 */
public class CreateQueryObjectBizLogic
{
	/**
	 * Gets the map which holds the data to create the rule object and add it to query.
	 * 
	 * @param strToCreateQueryObject
	 *            str to create query obj
	 * @param attrCollection            
	 * @return Map rules details
	 * @throws DynamicExtensionsSystemException
	 *             DynamicExtensionsSystemException
	 * @throws DynamicExtensionsApplicationException
	 *             DynamicExtensionsApplicationException
	 */
	public Map getRuleDetailsMap(String strToCreateQueryObject, Collection<AttributeInterface> attrCollection)
			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
	{
		Map ruleDetailsMap = new HashMap();
		if (attrCollection != null)
		{
			Map conditionsMap = createConditionsMap(strToCreateQueryObject);
			ruleDetailsMap = getEntityDetails(attrCollection, conditionsMap);
		}
		return ruleDetailsMap;
	}
	
	/** 
	 * This method get the entity details and populates the rule details map.
	 * @param attrCollection
	 * @param conditionsMap
	 * @return Map rules details
	 */
	private Map getEntityDetails(Collection<AttributeInterface> attrCollection, Map conditionsMap) 
	{
		String errorMessage = "";
		Map ruleDetailsMap = new HashMap();
		if (conditionsMap != null && !conditionsMap.isEmpty() && attrCollection != null
				&& !attrCollection.isEmpty())
		{
			List<AttributeInterface> attributes = new ArrayList<AttributeInterface>();
			List<String> attributeOperators = new ArrayList<String>();
			List<String> secondAttributeValues = new ArrayList<String>();
			ArrayList<ArrayList<String>> conditionValues = new ArrayList<ArrayList<String>>();
			String[] params;
			for(AttributeInterface attr : attrCollection)
			{
				params = paramsValue(conditionsMap, attr);
				if (params != null)
				{
					attributes.add(attr);
					attributeOperators.add(params[QueryModuleConstants.INDEX_PARAM_ZERO]);
					//firstAttributeValues.add(params[Constants.INDEX_PARAM_ONE]);
					secondAttributeValues.add(params[QueryModuleConstants.INDEX_PARAM_TWO]);
					ArrayList<String> attributeValues = getConditionValuesList(params);
					errorMessage = errorMessage
					+ validateAttributeValues(attr.getDataType().trim(), attributeValues);
					if("".equals(errorMessage))
					{
						if (QueryModuleConstants.Between.equals(params[QueryModuleConstants
						                                               .INDEX_PARAM_ZERO]))
						{
							attributeValues = Utility.getAttributeValuesInProperOrder(attr
							.getDataType(),attributeValues.get(QueryModuleConstants
							.ARGUMENT_ZERO), attributeValues.get(1));
						}
						conditionValues.add(attributeValues);
					}
				}
			}
			if("".equals(errorMessage))
			{
				ruleDetailsMap.put(AppletConstants.ATTRIBUTES, attributes);
				ruleDetailsMap.put(AppletConstants.ATTRIBUTE_OPERATORS, attributeOperators);
				//ruleDetailsMap.put(AppletConstants.FIRST_ATTR_VALUES, firstAttributeValues);
				ruleDetailsMap.put(AppletConstants.SECOND_ATTR_VALUES, secondAttributeValues);
				ruleDetailsMap.put(AppletConstants.ATTR_VALUES, conditionValues);
			}
			ruleDetailsMap.put(AppletConstants.ERROR_MESSAGE, errorMessage);
		}
	       return ruleDetailsMap;
	}

	/** 
	 * This method get the name and Id of the component.
	 * @param conditionsMap
	 * @param attr
	 * @return String
	 */
	private String[] paramsValue(Map conditionsMap, AttributeInterface attr)
	{
		String componentId = attr.getName() + attr.getId().toString();
		String[] params = (String[]) conditionsMap.get(componentId);
		return params;
	}

	/**
	 * @param params
	 * @return ArrayList attributeValues
	 */
	private ArrayList<String> getConditionValuesList(String[] params)
	{
		ArrayList<String> attributeValues = new ArrayList<String>();
		if (params[1] != null)
		{
				String[] values = params[1].split(QueryModuleConstants.QUERY_VALUES_DELIMITER);
				int len = values.length;
				for (int i = 0; i < len; i++)
				{
					if(!"".equals(values[i]))
					attributeValues.add(values[i].trim());
				}
		}
		if (params[2] != null)
		{
			attributeValues.add(params[2].trim());
		}
		return attributeValues;
	}

	/**
	 * Validates the user input and populates the list of messages to be shown
	 * to the user on the screen.
	 * 
	 * @param dataType
	 *            String
	 * @param attrvalues
	 *            List<String>
	 * @return String message
	 */
	private String validateAttributeValues(String dataType, List<String> attrvalues)
	{
		Validator validator = new Validator();
		String errorMessages = "";
		for(String enteredValue : attrvalues)
		{
			if (Constants.MISSING_TWO_VALUES.equals(enteredValue))
			{
				errorMessages = getErrorMessageForBetweenOperator(errorMessages, enteredValue);
			}
			else if ((QueryModuleConstants.BIG_INT.equals(dataType) 
					|| QueryModuleConstants.INTEGER.equals(dataType))
					|| QueryModuleConstants.LONG.equals(dataType))
			{
				Logger.out.debug(" Check for integer");
				if (validator.convertToLong(enteredValue) == null)
				{
					errorMessages = errorMessages + ApplicationProperties
					.getValue("simpleQuery.intvalue.required");
					Logger.out.debug(enteredValue + " is not a valid integer");
				}
				else if (!validator.isPositiveNumeric(enteredValue, QueryModuleConstants.ARGUMENT_ZERO))
				{
					errorMessages = getErrorMessageForPositiveNum(errorMessages, enteredValue);
				}

			}// integer
			else if ((QueryModuleConstants.DOUBLE.equals(dataType)) && !validator
					.isDouble(enteredValue, false))
			{
				errorMessages = errorMessages + ApplicationProperties
				.getValue("simpleQuery.decvalue.required");
			} // double
			else if (QueryModuleConstants.TINY_INT.equals(dataType))
			{
				if (!QueryModuleConstants.BOOLEAN_YES.equals(enteredValue.trim())
						&& !QueryModuleConstants.BOOLEAN_NO.equals(enteredValue.trim()))
				{
					errorMessages = errorMessages + ApplicationProperties
					.getValue("simpleQuery.tinyint.format");
				}
			}
			else if (Constants.FIELD_TYPE_TIMESTAMP_TIME.equals(dataType))
			{
				errorMessages = getErrorMessageForTimeFormat(validator, errorMessages, enteredValue);
			}
			else if (Constants.FIELD_TYPE_DATE.equals(dataType)
					|| Constants.FIELD_TYPE_TIMESTAMP_DATE.equals(dataType))
			{
				errorMessages = getErrorMessageForDateFormat(validator, errorMessages, enteredValue);
			}
		}
		return errorMessages;
	}

	/**
	 * This methods returns error message for Positive Number.
	 * @param errorMessages
	 * @param enteredValue
	 * @return string Message
	 */
	private String getErrorMessageForPositiveNum(String errorMessages, String enteredValue)
	{
		errorMessages = errorMessages + ApplicationProperties
		.getValue("simpleQuery.intvalue.poisitive.required");
		Logger.out.debug(enteredValue + " is not a positive integer");
		return errorMessages;
	}

	/** 
	 * This methods returns error message for between operator.
	 * @param errorMessages
	 * @param enteredValue
	 * @return String Message
	 */
	private String getErrorMessageForBetweenOperator(String errorMessages, String enteredValue)
	{
		errorMessages = errorMessages + ApplicationProperties.getValue("simpleQuery.twovalues.required");
		Logger.out.debug(enteredValue + " two values required for 'Between' operator ");
		return errorMessages;
	}

	/**
	 * This methods returns error message for Time Format.
	 * @param validator
	 * @param errorMessages
	 * @param enteredValue
	 * @return String Message
	 */
	private String getErrorMessageForTimeFormat(Validator validator, String errorMessages, String enteredValue)
	{
		if (!validator.isValidTime(enteredValue, Constants.TIME_PATTERN_HH_MM_SS))
		{
			errorMessages = errorMessages + ApplicationProperties.getValue("simpleQuery.time.format");
		}
		return errorMessages;
	}

	/**
	 * This methods returns error message for date Format.
	 * @param validator
	 * @param errorMessages
	 * @param enteredValue
	 * @return String Message
	 */
	private String getErrorMessageForDateFormat(Validator validator, String errorMessages, String enteredValue)
	{
		if (!validator.checkDate(enteredValue))
		{
			errorMessages = errorMessages + ApplicationProperties.getValue("simpleQuery.date.format");
		}
		return errorMessages;
	}

	/**
	 * Craetes Map of condition Objects.
	 * 
	 * @param queryString
	 *            queryString
	 * @return Map conditions map
	 * @throws DynamicExtensionsApplicationException
	 *             DynamicExtensionsApplicationException
	 * @throws DynamicExtensionsSystemException
	 *             DynamicExtensionsSystemException
	 */
	public Map<String, String[]> createConditionsMap(String queryString)
	{
		Map<String, String[]> conditionsMap = new HashMap<String, String[]>();
		String[] conditions = queryString.split(QueryModuleConstants.QUERY_CONDITION_DELIMITER);
		String[] attrParams;
		String condition;
		int len= conditions.length;
		for (int i = 0; i < len; i++)
		{
			attrParams = new String[QueryModuleConstants.INDEX_LENGTH];
			condition = conditions[i];
			if (!condition.equals(""))
			{
				condition = condition.substring(QueryModuleConstants.ARGUMENT_ZERO,
						condition.indexOf(QueryModuleConstants.ENTITY_SEPARATOR));
				String attrName = null;
				StringTokenizer tokenizer = new StringTokenizer(condition,
						QueryModuleConstants.QUERY_OPERATOR_DELIMITER);
				while (tokenizer.hasMoreTokens())
				{
					attrName = tokenizer.nextToken();
					if (tokenizer.hasMoreTokens())
					{
						String operator = tokenizer.nextToken();
						attrParams[QueryModuleConstants.INDEX_PARAM_ZERO] = operator;
						if (tokenizer.hasMoreTokens())
						{
							attrParams[1] = tokenizer.nextToken();
							if (RelationalOperator.Between.toString().equals(operator))
							{
								attrParams[QueryModuleConstants.INDEX_PARAM_TWO]
								= tokenizer.nextToken();
							}
						}
					}
				}
				conditionsMap.put(attrName, attrParams);
			}
		}
		return conditionsMap;
	}


	/**
	 * This method process the input values for the conditions and set it to the conditions in the query
	 * also replaces the conditions with the parameterized conditions
	 * @param queryInputString
	 * @param constraints
	 * @param displayNamesMap
	 * @return String Message
	 */
	public String setInputDataToQuery(String queryInputString, IConstraints constraints,
			Map<String, String> displayNamesMap)
	{
		String errorMessage = "";
		Map<String, String[]> newConditions = null;
		if (queryInputString != null)
		{
			newConditions = createConditionsMap(queryInputString);
		}
		Enumeration<IExpressionId> expressionIds = constraints.getExpressionIds();
		IExpression expression;
		while (expressionIds.hasMoreElements())
		{
			expression = constraints.getExpression(expressionIds.nextElement());
			int no_of_oprds = expression.numberOfOperands();
			IExpressionOperand operand;
			for (int i = 0; i < no_of_oprds; i++)
			{
				operand = expression.getOperand(i);
				if (!operand.isSubExpressionOperand())
				{
					if(operand instanceof IRule)
					{
						int expId = expression.getExpressionId().getInt();
						errorMessage = componentValues(displayNamesMap, errorMessage,
							newConditions, expId,  ((IRule) operand).getConditions());
					}
					else
					{
						errorMessage = "Could not save Temporal Query, as this feature is not yet Implemented";
					}
				}
			}
		}
		return errorMessage;
	}

	/**
	 * @param displayNamesMap
	 * @param errorMessage
	 * @param newConditions
	 * @param expId
	 * @param conditions
	 * @return String Message
	 */
	private String componentValues(Map<String, String> displayNamesMap, String errorMessage,
			Map<String, String[]> newConditions, int expId, List<ICondition> conditions)
	{
		ICondition condition;
		String componentName;
		int size = conditions.size();
		for (int j = 0; j < size; j++)
		{
			condition = conditions.get(j);
			componentName = generateComponentName(expId, condition.getAttribute());
			if (newConditions != null && newConditions.containsKey(componentName))
			{
				String[] params = newConditions.get(componentName);
				ArrayList<String> attributeValues = getConditionValuesList(params);
				errorMessage = errorMessage + validateAttributeValues(condition
						.getAttribute().getDataType().toString(),attributeValues);
				condition.setValues(attributeValues);
				condition.setRelationalOperator(RelationalOperator.getOperatorForStringRepresentation(
						params[QueryModuleConstants.INDEX_PARAM_ZERO]));
			}
			if (displayNamesMap != null && displayNamesMap.containsKey(componentName))
			{
				IParameterizedCondition iparameterizedCondition
				= new ParameterizedCondition(condition);
				iparameterizedCondition.setName(displayNamesMap.get(componentName));
				conditions.set(j, iparameterizedCondition);
			}
		}
		return errorMessage;
	}

	/**
	 * This Method generates component name as expressionId_attributeId.
	 */
	private String generateComponentName(int expressionId, AttributeInterface attribute) {
		String componentId = expressionId + QueryModuleConstants.UNDERSCORE + attribute.getId().toString();
		return componentId;
	}
}