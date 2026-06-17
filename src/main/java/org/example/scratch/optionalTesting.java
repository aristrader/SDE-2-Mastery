package org.example.scratch;

import java.util.Optional;

public class optionalTesting {
  public static void main(String[] args) {

    System.out.println("This".equalsIgnoreCase(null));

    System.out.println("THIS..IS..A..TEST".equals(null));

    Optional<String> xyz = Optional.ofNullable(null);
//    Optional.ofNullable()
    if(xyz.isPresent()){
      System.out.println("Somethign found");
    } else {
      System.out.println("Nothing found");
    }
  }
}

//if(apiResponse.getCard()!=null && apiResponse.getCard().getType().equalsIgnoreCase(TYPE_MY_KAD)){
//    apiResponse.setHasError(true);
//    apiResponse.setException(new MyServiceException(MyServiceErrorCode.CC_CARD_MISMATCH, verifyRequest.getOperations()));
//    }

//if (checkIfUnknownCardDetected(cardClassifierResult)) {
//exception = new MyServiceException(MyServiceErrorCode.CC_CARD_MISMATCH, request.getOperationSet());
//    } else {
//exception = this.translateCodeToException(cardClassifierResult);
//            }

//public boolean checkIfUnknownCardDetected(CardClassifierResult cardClassifierResult){
//  if(cardClassifierResult.getCode() != null && SUCCESS_CODE.equals(cardClassifierResult.getCode())){
//    if(cardClassifierResult.getCardClassifierContainer() != null &&
//        cardClassifierResult.getCardClassifierContainer().getCardTypeResult() != null){
//      return cardClassifierResult.getCardClassifierContainer().getCardTypeResult()
//          .getType().contains(UNKNOWN_CARD);
//    }
//  }
//  return false;
//}

