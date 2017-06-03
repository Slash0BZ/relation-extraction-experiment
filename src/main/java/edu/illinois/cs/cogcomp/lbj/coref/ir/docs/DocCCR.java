package edu.illinois.cs.cogcomp.lbj.coref.ir.docs;

import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.solutions.ChainSolution;

import java.util.*;

/**
 * Created by ngupta19 on 12/24/15.
 */
public class DocCCR extends DocPlainText implements Doc {
    private Map<Integer, ChainSolution<Mention>> corefChainMap; // Int-CorefChain Map to store all coref chains in the document

    private Map<Integer, Set<Mention>> canonicalMentions;   // Map - Int (CorefChain) to its canonical Mentions (Could be all NAM mentions in chain.


    private static final long serialVersionUID = 1L;


    public DocCCR(){
        setup();
    }

    public void setup(){
        corefChainMap = new HashMap<Integer, ChainSolution<Mention>>();
        canonicalMentions = new HashMap<Integer, Set<Mention>>();

    }

    /**
     * Todo : DOES NOT INCLUDE A CHECK TO SEE IF THE COREF CHAIN ALREADY EXISTS WITH SOME OTHER KEY to Avoid Duplication
     * @param chainKey Key of chain being added. Should not exist earlier
     * @param chain Coref chain being added to the given key.
     * @return
     */
    public void addcorefChainToMap(int chainKey, ChainSolution<Mention> chain){
        if(corefChainMap == null){
            System.err.println("Coref Chain Map of Document not initialized");
            System.exit(0);
        }

        if(corefChainMap.containsKey(chainKey)){
            System.err.println("Chain with given key already exisits");
            System.exit(0);
        }

        corefChainMap.put(chainKey, chain);
    }


    /**\
     *
     * @param chainKey Int key of coref chain for which canonical mention is being added.
     * @param m Canonical mention being added
     */
    public void addCanonicalMention(int chainKey, Mention m){
        int key = chainKey;
        if(canonicalMentions == null){
            System.err.println("Canonical Mention Map of Document not initialized");
            System.exit(0);
        }
        if(!corefChainMap.containsKey(key)){
            System.err.println("Key Error : Coref Chain with this key does not exist");
            System.exit(0);
        }
        if(m == null){
            System.err.println("ERROR : Input Mention is NULL");
            System.exit(0);
        }

        if(!canonicalMentions.containsKey(key)){
            canonicalMentions.put(key, new HashSet<Mention>());
        }

        canonicalMentions.get(key).add(m);
    }

    public int numTotalChains() { return this.corefChainMap.keySet().size(); }
    public int numRelevantChains() { return this.canonicalMentions.keySet().size(); }

    // Todo : Remove function, too dangerous to supply!!
    public Map<Integer, ChainSolution<Mention>> getCorefChainMap(){
        return this.corefChainMap;
    }

    public Set<Integer> getCorefChainsKeySet(){ return this.corefChainMap.keySet(); }

    public ChainSolution<Mention> getCorefChainfromKey(int chainKey){
        if(!this.corefChainMap.containsKey(chainKey) || this.corefChainMap == null){
            System.err.println("ERROR : Coref Chain Key not found");
            System.exit(0);
        }
        return this.corefChainMap.get(chainKey);
    }


//    public HashSet<Mention> getCanonicalMentions(int key){
//        if(!this.canonicalMentions.containsKey(key) || this.canonicalMentions == null){
//            System.err.println("ERROR : Canonical Mentions for given key not found");
//            System.exit(0);
//        }
//        return this.canonicalMentions.get(key);
//    }

    public Set<Mention> getCanonicalMentions(int key){
        if(this.canonicalMentions == null){
            System.err.println("ERROR : Canonical Mentions set not initialized");
            System.exit(0);
        }
        if(!this.corefChainMap.containsKey(Integer.valueOf(key))){
            System.err.println("ERROR :  INPUT CHAIN KEY IS NOT EVEN A VALID COREF CHAIN KEY, LET ALONE BEGIN RELEVANT.");
            System.exit(0);
        }

        if(!this.canonicalMentions.containsKey(Integer.valueOf(key))){
            Set<Mention> can = new HashSet<Mention>();
            return can;
        }

        return this.canonicalMentions.get(key);
    }

    public Set<Mention> getAllCanonicalMentionsForDoc(){
        Set<Mention> allCanonicals = new HashSet<Mention>();
        for(int key : this.canonicalMentions.keySet()){
            allCanonicals.addAll(this.canonicalMentions.get(key));
        }
        return allCanonicals;
    }


    public Set<Integer> getRelevantChainKeySet(){ return this.canonicalMentions.keySet(); }

}
