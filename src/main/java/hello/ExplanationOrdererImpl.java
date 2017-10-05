package hello;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//



import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLPropertyAxiom;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLRuntimeException;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.util.CollectionFactory;
import org.semanticweb.owlapi.util.OWLAPIPreconditions;
import org.semanticweb.owlapi.util.OWLAxiomVisitorAdapter;
import uk.ac.manchester.cs.owl.explanation.ordering.EntailedAxiomTree;
import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationOrderer;
import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationTree;
import uk.ac.manchester.cs.owl.explanation.ordering.Tree;

public class ExplanationOrdererImpl implements ExplanationOrderer {
    private Set<OWLAxiom> currentExplanation = CollectionFactory.emptySet();
    @Nonnull
    private final Map<OWLEntity, Set<OWLAxiom>> lhs2AxiomMap = new HashMap();
    @Nonnull
    private final Map<OWLAxiom, Set<OWLEntity>> entitiesByAxiomRHS = new HashMap();
    @Nonnull
    private final ExplanationOrdererImpl.SeedExtractor seedExtractor = new ExplanationOrdererImpl.SeedExtractor();
    @Nonnull
    private final OWLOntologyManager man;
    private OWLOntology ont;
    @Nonnull
    private final Map<OWLObject, Set<OWLAxiom>> mappedAxioms = new HashMap();
    @Nonnull
    private final Set<OWLAxiom> consumedAxioms = new HashSet();
    @Nonnull
    private final Set<AxiomType<?>> passTypes = new HashSet();
    @Nonnull
    private static final Comparator<Tree<OWLAxiom>> COMPARATOR = new ExplanationOrdererImpl.OWLAxiomTreeComparator();
    private static final AtomicLong RANDOMSTART = new AtomicLong(System.currentTimeMillis());
    private static final ExplanationOrdererImpl.PropertiesFirstComparator PROPERTIESFIRSTCOMPARATOR = new ExplanationOrdererImpl.PropertiesFirstComparator();

    public ExplanationOrdererImpl(@Nonnull OWLOntologyManager m) {
        this.man = (OWLOntologyManager)OWLAPIPreconditions.checkNotNull(m, "m cannot be null");
        this.passTypes.add(AxiomType.DISJOINT_CLASSES);
    }

    private void reset() {
        this.lhs2AxiomMap.clear();
        this.entitiesByAxiomRHS.clear();
        this.consumedAxioms.clear();
    }

    public ExplanationTree getOrderedExplanation(@Nonnull OWLAxiom entailment, @Nonnull Set<OWLAxiom> axioms) {
        this.currentExplanation = new HashSet(axioms);
        this.buildIndices();
        EntailedAxiomTree root = new EntailedAxiomTree(entailment);
        this.insertChildren(this.seedExtractor.getSource(entailment), root);
        OWLEntity currentTarget = this.seedExtractor.getTarget(entailment);
        Set axs = root.getUserObjectClosure();
        ArrayList rootAxioms = new ArrayList();
        Iterator var7 = axioms.iterator();

        OWLAxiom ax;
        while(var7.hasNext()) {
            ax = (OWLAxiom)var7.next();
            if(!axs.contains(ax)) {
                rootAxioms.add(ax);
            }
        }

        Collections.sort(rootAxioms, new ExplanationOrdererImpl.TargetAxiomsComparator(this.getTargetAxioms(currentTarget)));
        var7 = rootAxioms.iterator();

        while(var7.hasNext()) {
            ax = (OWLAxiom)var7.next();

            assert ax != null;

            root.addChild(new ExplanationTree(ax));
        }

        return root;
    }

    @Nonnull
    private Set<OWLAxiom> getTargetAxioms(@Nonnull OWLEntity currentTarget) {
        HashSet targetAxioms = new HashSet();
        if(currentTarget.isOWLClass()) {
            targetAxioms.addAll(this.ont.getAxioms(currentTarget.asOWLClass(), Imports.EXCLUDED));
        }

        if(currentTarget.isOWLObjectProperty()) {
            targetAxioms.addAll(this.ont.getAxioms(currentTarget.asOWLObjectProperty(), Imports.EXCLUDED));
        }

        if(currentTarget.isOWLDataProperty()) {
            targetAxioms.addAll(this.ont.getAxioms(currentTarget.asOWLDataProperty(), Imports.EXCLUDED));
        }

        if(currentTarget.isOWLNamedIndividual()) {
            targetAxioms.addAll(this.ont.getAxioms(currentTarget.asOWLNamedIndividual(), Imports.EXCLUDED));
        }

        return targetAxioms;
    }

    @Nonnull
    private List<OWLEntity> getRHSEntitiesSorted(@Nonnull OWLAxiom ax) {
        Collection entities = this.getRHSEntities(ax);
        ArrayList sortedEntities = new ArrayList(entities);
        Collections.sort(sortedEntities, PROPERTIESFIRSTCOMPARATOR);
        return sortedEntities;
    }

    private void insertChildren(@Nullable OWLEntity entity, @Nonnull ExplanationTree tree) {
        if(entity != null) {
            HashSet currentPath = new HashSet(tree.getUserObjectPathToRoot());
            Set axioms = CollectionFactory.emptySet();
            if(entity.isOWLClass()) {
                axioms = this.ont.getAxioms(entity.asOWLClass(), Imports.EXCLUDED);
            } else if(entity.isOWLObjectProperty()) {
                axioms = this.ont.getAxioms(entity.asOWLObjectProperty(), Imports.EXCLUDED);
            } else if(entity.isOWLDataProperty()) {
                axioms = this.ont.getAxioms(entity.asOWLDataProperty(), Imports.EXCLUDED);
            } else if(entity.isOWLNamedIndividual()) {
                axioms = this.ont.getAxioms(entity.asOWLNamedIndividual(), Imports.EXCLUDED);
            }

            Iterator var5 = axioms.iterator();

            while(true) {
                OWLAxiom ax;
                Set mapped;
                do {
                    do {
                        do {
                            do {
                                if(!var5.hasNext()) {
                                    sortChildrenAxioms(tree);
                                    return;
                                }

                                ax = (OWLAxiom)var5.next();
                            } while(this.passTypes.contains(ax.getAxiomType()));

                            mapped = getIndexedSet(entity, this.mappedAxioms, true);
                        } while(this.consumedAxioms.contains(ax));
                    } while(mapped.contains(ax));
                } while(currentPath.contains(ax));

                mapped.add(ax);
                this.consumedAxioms.add(ax);
                ExplanationTree child = new ExplanationTree(ax);
                tree.addChild(child);
                Iterator var9 = this.getRHSEntitiesSorted(ax).iterator();

                while(var9.hasNext()) {
                    OWLEntity ent = (OWLEntity)var9.next();

                    assert ent != null;

                    this.insertChildren(ent, child);
                }
            }
        }
    }

    private static void sortChildrenAxioms(@Nonnull ExplanationTree tree) {
        tree.sortChildren(COMPARATOR);
    }

    private void buildIndices() {
        this.reset();
        ExplanationOrdererImpl.AxiomMapBuilder builder = new ExplanationOrdererImpl.AxiomMapBuilder();
        Iterator e = this.currentExplanation.iterator();

        while(e.hasNext()) {
            OWLAxiom ax = (OWLAxiom)e.next();
            ax.accept(builder);
        }

        try {
            if(this.ont != null) {
                this.man.removeOntology((OWLOntology)OWLAPIPreconditions.verifyNotNull(this.ont));
            }

            this.ont = this.man.createOntology(IRI.create("http://www.semanticweb.org/", "ontology" + RANDOMSTART.incrementAndGet()));
            ArrayList e1 = new ArrayList();
            Iterator ax2 = this.currentExplanation.iterator();

            while(ax2.hasNext()) {
                OWLAxiom ax1 = (OWLAxiom)ax2.next();

                assert ax1 != null;

                e1.add(new AddAxiom((OWLOntology)OWLAPIPreconditions.verifyNotNull(this.ont), ax1));
                ax1.accept(builder);
            }

            this.man.applyChanges(e1);
        } catch (OWLOntologyCreationException var5) {
            throw new OWLRuntimeException(var5);
        }
    }

    @Nonnull
    private static <K, E> Set<E> getIndexedSet(@Nonnull K obj, @Nonnull Map<K, Set<E>> map, boolean addIfEmpty) {
        Object values = (Set)map.get(obj);
        if(values == null) {
            values = new HashSet();
            if(addIfEmpty) {
                map.put(obj, (Set<E>) values);
            }
        }

        return (Set)values;
    }

    @Nonnull
    protected Set<OWLAxiom> getAxiomsForLHS(@Nonnull OWLEntity lhs) {
        return getIndexedSet(lhs, this.lhs2AxiomMap, true);
    }

    @Nonnull
    private Collection<OWLEntity> getRHSEntities(@Nonnull OWLAxiom axiom) {
        return getIndexedSet(axiom, this.entitiesByAxiomRHS, true);
    }

    protected void indexAxiomsByRHSEntities(@Nonnull OWLObject rhs, @Nonnull OWLAxiom axiom) {
        getIndexedSet(axiom, this.entitiesByAxiomRHS, true).addAll(rhs.getSignature());
    }

    private class AxiomMapBuilder extends OWLAxiomVisitorAdapter {
        AxiomMapBuilder() {
        }

        public void visit(OWLSubClassOfAxiom axiom) {
            if(!axiom.getSubClass().isAnonymous()) {
                ExplanationOrdererImpl.this.getAxiomsForLHS(axiom.getSubClass().asOWLClass()).add(axiom);
                ExplanationOrdererImpl.this.indexAxiomsByRHSEntities(axiom.getSuperClass(), axiom);
            }

        }

        public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
            if(!((OWLObjectPropertyExpression)axiom.getProperty()).isAnonymous()) {
                ExplanationOrdererImpl.this.getAxiomsForLHS(((OWLObjectPropertyExpression)axiom.getProperty()).asOWLObjectProperty()).add(axiom);
            }

        }

        public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
            if(!((OWLObjectPropertyExpression)axiom.getProperty()).isAnonymous()) {
                ExplanationOrdererImpl.this.getAxiomsForLHS(((OWLObjectPropertyExpression)axiom.getProperty()).asOWLObjectProperty()).add(axiom);
            }

        }

        public void visit(OWLDisjointClassesAxiom axiom) {
            OWLClassExpression desc;
            for(Iterator var2 = axiom.getClassExpressions().iterator(); var2.hasNext(); ExplanationOrdererImpl.this.indexAxiomsByRHSEntities(desc, axiom)) {
                desc = (OWLClassExpression)var2.next();
                if(!desc.isAnonymous()) {
                    ExplanationOrdererImpl.this.getAxiomsForLHS(desc.asOWLClass()).add(axiom);
                }
            }

        }

        public void visit(OWLDataPropertyDomainAxiom axiom) {
            ExplanationOrdererImpl.this.getAxiomsForLHS(((OWLDataPropertyExpression)axiom.getProperty()).asOWLDataProperty()).add(axiom);
            ExplanationOrdererImpl.this.indexAxiomsByRHSEntities(axiom.getDomain(), axiom);
        }

        public void visit(OWLObjectPropertyDomainAxiom axiom) {
            if(!((OWLObjectPropertyExpression)axiom.getProperty()).isAnonymous()) {
                ExplanationOrdererImpl.this.getAxiomsForLHS(((OWLObjectPropertyExpression)axiom.getProperty()).asOWLObjectProperty()).add(axiom);
            }

            ExplanationOrdererImpl.this.indexAxiomsByRHSEntities(axiom.getDomain(), axiom);
        }

        public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
            OWLObjectPropertyExpression prop;
            for(Iterator var2 = axiom.getProperties().iterator(); var2.hasNext(); ExplanationOrdererImpl.this.indexAxiomsByRHSEntities(prop, axiom)) {
                prop = (OWLObjectPropertyExpression)var2.next();
                if(!prop.isAnonymous()) {
                    ExplanationOrdererImpl.this.getAxiomsForLHS(prop.asOWLObjectProperty()).add(axiom);
                }
            }

        }

        public void visit(OWLDifferentIndividualsAxiom axiom) {
            Iterator var2 = axiom.getIndividuals().iterator();

            while(var2.hasNext()) {
                OWLIndividual ind = (OWLIndividual)var2.next();
                if(!ind.isAnonymous()) {
                    ExplanationOrdererImpl.this.getAxiomsForLHS(ind.asOWLNamedIndividual()).add(axiom);
                    ExplanationOrdererImpl.this.indexAxiomsByRHSEntities(ind, axiom);
                }
            }

        }

        public void visit(OWLDisjointDataPropertiesAxiom axiom) {
            Iterator var2 = axiom.getProperties().iterator();

            while(var2.hasNext()) {
                OWLDataPropertyExpression prop = (OWLDataPropertyExpression)var2.next();
                ExplanationOrdererImpl.this.getAxiomsForLHS(prop.asOWLDataProperty()).add(axiom);
                ExplanationOrdererImpl.this.indexAxiomsByRHSEntities(prop, axiom);
            }

        }

        public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
            OWLObjectPropertyExpression prop;
            for(Iterator var2 = axiom.getProperties().iterator(); var2.hasNext(); ExplanationOrdererImpl.this.indexAxiomsByRHSEntities(prop, axiom)) {
                prop = (OWLObjectPropertyExpression)var2.next();
                if(!prop.isAnonymous()) {
                    ExplanationOrdererImpl.this.getAxiomsForLHS(prop.asOWLObjectProperty()).add(axiom);
                }
            }

        }

        public void visit(OWLObjectPropertyRangeAxiom axiom) {
            if(!((OWLObjectPropertyExpression)axiom.getProperty()).isAnonymous()) {
                ExplanationOrdererImpl.this.getAxiomsForLHS(((OWLObjectPropertyExpression)axiom.getProperty()).asOWLObjectProperty()).add(axiom);
            }

            ExplanationOrdererImpl.this.indexAxiomsByRHSEntities(axiom.getRange(), axiom);
        }

        public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
            if(!((OWLObjectPropertyExpression)axiom.getProperty()).isAnonymous()) {
                ExplanationOrdererImpl.this.getAxiomsForLHS(((OWLObjectPropertyExpression)axiom.getProperty()).asOWLObjectProperty()).add(axiom);
            }

        }

        public void visit(OWLSubObjectPropertyOfAxiom axiom) {
            if(!((OWLObjectPropertyExpression)axiom.getSubProperty()).isAnonymous()) {
                ExplanationOrdererImpl.this.getAxiomsForLHS(((OWLObjectPropertyExpression)axiom.getSubProperty()).asOWLObjectProperty()).add(axiom);
            }

            ExplanationOrdererImpl.this.indexAxiomsByRHSEntities(axiom.getSuperProperty(), axiom);
        }

        public void visit(OWLDisjointUnionAxiom axiom) {
            ExplanationOrdererImpl.this.getAxiomsForLHS(axiom.getOWLClass()).add(axiom);
        }

        public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
            if(!((OWLObjectPropertyExpression)axiom.getProperty()).isAnonymous()) {
                ExplanationOrdererImpl.this.getAxiomsForLHS(((OWLObjectPropertyExpression)axiom.getProperty()).asOWLObjectProperty()).add(axiom);
            }

        }

        public void visit(OWLDataPropertyRangeAxiom axiom) {
            if(!((OWLDataPropertyExpression)axiom.getProperty()).isAnonymous()) {
                ExplanationOrdererImpl.this.getAxiomsForLHS(((OWLDataPropertyExpression)axiom.getProperty()).asOWLDataProperty()).add(axiom);
            }

            ExplanationOrdererImpl.this.indexAxiomsByRHSEntities(axiom.getRange(), axiom);
        }

        public void visit(OWLFunctionalDataPropertyAxiom axiom) {
            if(!((OWLDataPropertyExpression)axiom.getProperty()).isAnonymous()) {
                ExplanationOrdererImpl.this.getAxiomsForLHS(((OWLDataPropertyExpression)axiom.getProperty()).asOWLDataProperty()).add(axiom);
            }

        }

        public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
            Iterator var2 = axiom.getProperties().iterator();

            while(var2.hasNext()) {
                OWLDataPropertyExpression prop = (OWLDataPropertyExpression)var2.next();
                ExplanationOrdererImpl.this.getAxiomsForLHS(prop.asOWLDataProperty()).add(axiom);
                ExplanationOrdererImpl.this.indexAxiomsByRHSEntities(prop, axiom);
            }

        }

        public void visit(OWLClassAssertionAxiom axiom) {
            if(!axiom.getIndividual().isAnonymous()) {
                ExplanationOrdererImpl.this.getAxiomsForLHS(axiom.getIndividual().asOWLNamedIndividual()).add(axiom);
                ExplanationOrdererImpl.this.indexAxiomsByRHSEntities(axiom.getClassExpression(), axiom);
            }

        }

        public void visit(OWLEquivalentClassesAxiom axiom) {
            OWLClassExpression desc;
            for(Iterator var2 = axiom.getClassExpressions().iterator(); var2.hasNext(); ExplanationOrdererImpl.this.indexAxiomsByRHSEntities(desc, axiom)) {
                desc = (OWLClassExpression)var2.next();
                if(!desc.isAnonymous()) {
                    ExplanationOrdererImpl.this.getAxiomsForLHS(desc.asOWLClass()).add(axiom);
                }
            }

        }

        public void visit(OWLDataPropertyAssertionAxiom axiom) {
            ExplanationOrdererImpl.this.indexAxiomsByRHSEntities(axiom.getSubject(), axiom);
        }

        public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
            if(!((OWLObjectPropertyExpression)axiom.getProperty()).isAnonymous()) {
                ExplanationOrdererImpl.this.getAxiomsForLHS(((OWLObjectPropertyExpression)axiom.getProperty()).asOWLObjectProperty()).add(axiom);
            }

        }

        public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
            if(!((OWLObjectPropertyExpression)axiom.getProperty()).isAnonymous()) {
                ExplanationOrdererImpl.this.getAxiomsForLHS(((OWLObjectPropertyExpression)axiom.getProperty()).asOWLObjectProperty()).add(axiom);
            }

        }

        public void visit(OWLSubDataPropertyOfAxiom axiom) {
            ExplanationOrdererImpl.this.getAxiomsForLHS(((OWLDataPropertyExpression)axiom.getSubProperty()).asOWLDataProperty()).add(axiom);
            ExplanationOrdererImpl.this.indexAxiomsByRHSEntities(axiom.getSuperProperty(), axiom);
        }

        public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
            if(!((OWLObjectPropertyExpression)axiom.getProperty()).isAnonymous()) {
                ExplanationOrdererImpl.this.getAxiomsForLHS(((OWLObjectPropertyExpression)axiom.getProperty()).asOWLObjectProperty()).add(axiom);
            }

        }

        public void visit(OWLSameIndividualAxiom axiom) {
            Iterator var2 = axiom.getIndividuals().iterator();

            while(var2.hasNext()) {
                OWLIndividual ind = (OWLIndividual)var2.next();
                if(!ind.isAnonymous()) {
                    ExplanationOrdererImpl.this.getAxiomsForLHS(ind.asOWLNamedIndividual()).add(axiom);
                    ExplanationOrdererImpl.this.indexAxiomsByRHSEntities(ind, axiom);
                }
            }

        }

        public void visit(OWLInverseObjectPropertiesAxiom axiom) {
            if(!axiom.getFirstProperty().isAnonymous()) {
                ExplanationOrdererImpl.this.getAxiomsForLHS(axiom.getFirstProperty().asOWLObjectProperty()).add(axiom);
            }

            ExplanationOrdererImpl.this.indexAxiomsByRHSEntities(axiom.getFirstProperty(), axiom);
            ExplanationOrdererImpl.this.indexAxiomsByRHSEntities(axiom.getSecondProperty(), axiom);
        }

        public void visit(OWLHasKeyAxiom axiom) {
            if(!axiom.getClassExpression().isAnonymous()) {
                ExplanationOrdererImpl.this.indexAxiomsByRHSEntities(axiom.getClassExpression().asOWLClass(), axiom);
            }

        }
    }

    private static class SeedExtractor extends OWLAxiomVisitorAdapter {
        private OWLEntity source;
        private OWLEntity target;

        SeedExtractor() {
        }

        @Nullable
        public OWLEntity getSource(@Nonnull OWLAxiom axiom) {
            axiom.accept(this);
            return this.source;
        }

        @Nonnull
        public OWLEntity getTarget(@Nonnull OWLAxiom axiom) {
            axiom.accept(this);
            if(target==null){
                target = axiom.getClassesInSignature().iterator().next();
            }
            return (OWLEntity)this.target;
        }

        public void visit(OWLSubClassOfAxiom axiom) {
            if(!axiom.getSubClass().isAnonymous()) {
                this.source = axiom.getSubClass().asOWLClass();
            }

            if(!axiom.getSuperClass().isOWLNothing()) {
                OWLClassExpression classExpression = axiom.getSuperClass();
                if(!classExpression.isAnonymous()) {
                    this.target = classExpression.asOWLClass();
                }
            }

        }

        public void visit(OWLDisjointClassesAxiom axiom) {
            Iterator var2 = axiom.getClassExpressions().iterator();

            while(var2.hasNext()) {
                OWLClassExpression ce = (OWLClassExpression)var2.next();
                if(!ce.isAnonymous()) {
                    if(this.source == null) {
                        this.source = ce.asOWLClass();
                    } else {
                        if(this.target != null) {
                            break;
                        }

                        this.target = ce.asOWLClass();
                    }
                }
            }

        }

        public void visit(OWLSubObjectPropertyOfAxiom axiom) {
            if(!((OWLObjectPropertyExpression)axiom.getSubProperty()).isAnonymous()) {
                this.source = ((OWLObjectPropertyExpression)axiom.getSubProperty()).asOWLObjectProperty();
            }

            if(!((OWLObjectPropertyExpression)axiom.getSuperProperty()).isAnonymous()) {
                this.target = ((OWLObjectPropertyExpression)axiom.getSuperProperty()).asOWLObjectProperty();
            }

        }

        public void visit(OWLClassAssertionAxiom axiom) {
            if(!axiom.getClassExpression().isAnonymous()) {
                this.source = axiom.getIndividual().asOWLNamedIndividual();
                this.target = axiom.getClassExpression().asOWLClass();
            }

        }

        public void visit(OWLEquivalentClassesAxiom axiom) {
            Iterator var2 = axiom.getNamedClasses().iterator();

            while(var2.hasNext()) {
                OWLClass cls = (OWLClass)var2.next();
                if(this.source == null) {
                    this.source = cls;
                } else {
                    if(this.target != null) {
                        break;
                    }

                    this.target = cls;
                }
            }

        }

        public void visit(SWRLRule rule) {
        }
    }

    private static class OWLAxiomTreeComparator implements Comparator<Tree<OWLAxiom>>, Serializable {
        private static final long serialVersionUID = 40000L;

        OWLAxiomTreeComparator() {
        }

        public int compare(Tree<OWLAxiom> o1, Tree<OWLAxiom> o2) {
            OWLAxiom ax1 = (OWLAxiom)o1.getUserObject();
            OWLAxiom ax2 = (OWLAxiom)o2.getUserObject();
            if(ax1 instanceof OWLEquivalentClassesAxiom) {
                return 1;
            } else if(ax2 instanceof OWLEquivalentClassesAxiom) {
                return -1;
            } else if(ax1 instanceof OWLPropertyAxiom) {
                return -1;
            } else {
                int diff = childDiff(o1, o2);
                if(diff != 0) {
                    return diff;
                } else if(ax1 instanceof OWLSubClassOfAxiom && ax2 instanceof OWLSubClassOfAxiom) {
                    OWLSubClassOfAxiom sc1 = (OWLSubClassOfAxiom)ax1;
                    OWLSubClassOfAxiom sc2 = (OWLSubClassOfAxiom)ax2;
                    return sc1.getSuperClass().compareTo(sc2.getSuperClass());
                } else {
                    return 1;
                }
            }
        }

        private static int childDiff(Tree<OWLAxiom> o1, Tree<OWLAxiom> o2) {
            int childCount1 = o1.getChildCount();
            childCount1 = childCount1 > 0?0:1;
            int childCount2 = o2.getChildCount();
            childCount2 = childCount2 > 0?0:1;
            return childCount1 - childCount2;
        }
    }

    private static class PropertiesFirstComparator implements Comparator<OWLObject>, Serializable {
        private static final long serialVersionUID = 40000L;

        PropertiesFirstComparator() {
        }

        public int compare(OWLObject o1, OWLObject o2) {
            return o1 instanceof OWLProperty?-1:(o1.equals(o2)?0:1);
        }
    }

    private static class TargetAxiomsComparator implements Comparator<OWLAxiom>, Serializable {
        private static final long serialVersionUID = 40000L;
        private final Set<OWLAxiom> targetAxioms;

        TargetAxiomsComparator(@Nonnull Set<OWLAxiom> targetAxioms) {
            this.targetAxioms = (Set)OWLAPIPreconditions.checkNotNull(targetAxioms, "targetAxioms cannot be null");
        }

        public int compare(OWLAxiom o1, OWLAxiom o2) {
            return this.targetAxioms.contains(o1)?1:(this.targetAxioms.contains(o2)?-1:0);
        }
    }
}
