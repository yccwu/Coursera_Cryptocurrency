import java.util.HashSet;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */

    private UTXOPool _utxo;

    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        this._utxo = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */


    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
        UTXOPool seenUTXO = new UTXOPool();
        double prevValSum = 0.0;
        double outputValSum = 0.0;

        Crypto sigValider = new Crypto();

        for(int i = 0; i < tx.numInputs(); i++){
            Transaction.Input currIn = tx.getInput(i);

            UTXO thisUTXO = new UTXO(currIn.prevTxHash, currIn.outputIndex);
            Transaction.Output OutFromPool = this._utxo.getTxOutput(thisUTXO);
            
            // (1)
            if(!this._utxo.contains(thisUTXO)){
                return false;
            }

            // (3)
            if(seenUTXO.contains(thisUTXO)){
                return false;
            }

            // (2)
            if(!sigValider.verifySignature(OutFromPool.address, tx.getRawDataToSign(i), currIn.signature)){
                return false;
            }

            prevValSum += OutFromPool.value;
            seenUTXO.addUTXO(thisUTXO, OutFromPool);
        }   // END of for-loop

        for(Transaction.Output currOut : tx.getOutputs()){
            // (4)
            if(currOut.value < 0){
                return false;
            }
            outputValSum += currOut.value;
        }   // END of for-loop

        // (5)
        return prevValSum >= outputValSum;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        HashSet<Transaction> vailds = new HashSet<Transaction>();

        for(Transaction thisTx : possibleTxs){
            if(isValidTx(thisTx)){
                vailds.add(thisTx); // unexcepted type

                for(Transaction.Input currIn: thisTx.getInputs()){
                    UTXO thisUTXO = new UTXO(currIn.prevTxHash, currIn.outputIndex);
                    if(this._utxo.contains(thisUTXO)){
                        this._utxo.removeUTXO(thisUTXO);
                    }
                }
                for(int i = 0; i < thisTx.numOutputs(); i++){
                    Transaction.Output CurrOut = thisTx.getOutput(i);
                    UTXO thisUTXO = new UTXO(thisTx.getHash(), i);
                    this._utxo.addUTXO(thisUTXO, CurrOut);
                }

            }   // END of if
        }   // END of for-loop

        return vailds.toArray(new Transaction[vailds.size()]);
    }

}
