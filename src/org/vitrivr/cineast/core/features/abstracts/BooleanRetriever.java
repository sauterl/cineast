package org.vitrivr.cineast.core.features.abstracts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.ProviderDataType;
import org.vitrivr.cineast.core.data.score.BooleanSegmentScoreElement;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.BooleanExpression;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.RelationalOperator;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.setup.EntityCreator;

public abstract class BooleanRetriever implements Retriever {

  private static final Logger LOGGER = LogManager.getLogger();
  protected DBSelector selector;
  protected final String entity;
  protected final HashSet<String> attributes = new HashSet<>();
  protected final HashMap<String, ProviderDataType> columnTypes = new HashMap<>();

  protected BooleanRetriever(String entity, Collection<String> attributes){
    this.entity = entity;
    this.attributes.addAll(attributes);
  }

  protected BooleanRetriever(Map<String, String> properties){
    if(!properties.containsKey("entity")){
      throw new RuntimeException("no entity specified in properties map of BooleanRetriever");
    }
    this.entity = properties.get("entity");

    if(properties.containsKey("attribute")){
      List<String> attrs = Arrays.stream(properties.get("attribute").split(",")).map(String::trim)
          .collect(
              Collectors.toList());
      this.attributes.addAll(attrs);
    }

  }

  protected abstract Collection<RelationalOperator> getSupportedOperators();

  @Override
  public void init(DBSelectorSupplier selectorSupply) {
    this.selector = selectorSupply.get();
    this.selector.open(entity);
  }

  public Collection<String> getAttributes(){
    return this.attributes.stream().map(x -> this.entity + "." + x).collect(Collectors.toSet());
  }

  protected boolean canProcess(BooleanExpression be){
    return getSupportedOperators().contains(be.getOperator()) && getAttributes().contains(be.getAttribute());
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {

    List<BooleanExpression> relevantExpressions = sc.getBooleanExpressions().stream().filter(this::canProcess).collect(
        Collectors.toList());

    if (relevantExpressions.isEmpty()){
      LOGGER.debug("No relevant expressions in {} for query {}", this.getClass().getSimpleName(), sc.toString());
      return Collections.emptyList();
    }

    return getMatching(relevantExpressions, qc);
  }

  protected List<ScoreElement> getMatching(List<BooleanExpression> expressions, ReadableQueryConfig qc){

    Set<String> relevantIds = null;

    for (BooleanExpression be: expressions){
      List<Map<String, PrimitiveTypeProvider>> rows = selector
          .getRows(be.getAttribute(), be.getOperator(), be.getValues().stream().map(
              PrimitiveTypeProvider::getString).collect(Collectors.toList()));

      if(rows.isEmpty()){
        return Collections.emptyList();
      }

      Set<String> ids = rows.stream().map(x -> x.get("id").getString())
          .collect(Collectors.toSet());

      Map<String, PrimitiveTypeProvider> firstRow = rows.get(0);
      firstRow.keySet().stream().forEach(x -> {
        if (!this.columnTypes.containsKey(x)){
          this.columnTypes.put(x, firstRow.get(x).getType());
        }
      });

      if(relevantIds == null){
        relevantIds = new HashSet<>(ids.size());
        relevantIds.addAll(ids);
      }else{
        relevantIds.retainAll(ids);
      }

    }

    if(relevantIds == null || relevantIds.isEmpty()){
      return Collections.emptyList();
    }
    
    return relevantIds.stream().map(BooleanSegmentScoreElement::new).collect(Collectors.toList());

  }

  @Override
  public List<ScoreElement> getSimilar(String shotId, ReadableQueryConfig qc) { //nop
    return Collections.emptyList();
  }

  @Override
  public void finish() {
    this.selector.close();
  }

  @Override
  public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
    //nop
  }

  @Override
  public void dropPersistentLayer(Supplier<EntityCreator> supply) {
    //nop
  }

  public ProviderDataType getColumnType(String column){
    return this.columnTypes.get(column);
  }
}
