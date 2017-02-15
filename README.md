# Protein Structure ANN
* [2-11] Working on the IO for the ANN. So far it is reading in 128 protein structures and saving the amino acid sequence as well as the target output to a "protein" object. will need to update this so that the IO is a sliding window of 17. maybe a scanner on the protein list? - cvhn

## IO.java File

## There are now three classes within this file:
### Protein
* This is the protein object. getWindow is here
### IO
* Handles the io for the project. builds proteins and partitions into appropriate datasets
### DataSets
* The three lists: train, tune, test