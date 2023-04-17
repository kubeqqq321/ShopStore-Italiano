package com.shopstore.online.serviceImpl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.shopstore.online.JWT.JwtFilter;
import com.shopstore.online.constens.CafeConstants;
import com.shopstore.online.dao.BillDao;
import com.shopstore.online.model.Bill;
import com.shopstore.online.service.BillService;
import com.shopstore.online.utils.CafeUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
public class BillServiceImpl implements BillService {

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    BillDao billDao;

    @Override
    public ResponseEntity<String> generateReport(Map<String, Object> requestMap) {

        try{
            String filename;
            if(validateRequestMap(requestMap)){
                if(requestMap.containsKey("isGenerate") && !(Boolean) requestMap.get("isGenerated")){
                    filename = (String) requestMap.get("uuid");
                }
                else{
                    filename = CafeUtils.getUUID();
                    requestMap.put("uuid" ,filename);
                    insertBill(requestMap);
                }

                String data = "Imię: "+requestMap.get("name")+"\n"+"Numer Telefonu: "+requestMap.get("contactNumber")+"\n"+
                                "Email: "+requestMap.get("email")+"\n"+"Metoda płatności: "+requestMap.get("paymentMethod");

                Document document = new Document();
                PdfWriter.getInstance(document,new FileOutputStream(CafeConstants.STORE_LOCATION+ "\\"+filename+".pdf"));

                document.open();
                setRectangleInPdf(document);

                Paragraph chunk = new Paragraph("ShopStore Cafe",getFont("Header"));

                chunk.setAlignment(Element.ALIGN_CENTER);
                document.add(chunk);

                Paragraph paragraph = new Paragraph(data+"\n \n",getFont("Data"));
                document.add(paragraph);

                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                addTableHeader(table);

                JSONArray jsonArray = CafeUtils.getJsonArrayFromString((String)requestMap.get("productDetails"));
                for (int i = 0; i<jsonArray.length() ;i++){
                    addRows(table,CafeUtils.getMapFromJson(jsonArray.getString(i)));
                }
                document.add(table);

                Paragraph footer = new Paragraph("Total: " + requestMap.get("totalAmount")+ "\n" +"Dziękuję za zakupy oraz zapraszam ponownie" , getFont("Data"));
                document.add(footer);
                document.close();
                return new ResponseEntity<>("{\"uuid\":\""+filename+"\"}" , HttpStatus.OK);


            }
            return CafeUtils.getResponseEntity("Nie znalezniono",HttpStatus.BAD_REQUEST);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void addRows(PdfPTable table, Map<String, Object> data) {
        log.info("inside addrow");
        table.addCell((String)data.get("name"));
        table.addCell((String)data.get("category"));
        table.addCell((String)data.get("quantity"));
        table.addCell(Double.toString((Double) data.get("price")));
        table.addCell(Double.toString((Double) data.get("total")));
    }


    private void addTableHeader(PdfPTable table) {
        log.info("Inside addTableHeader");
        Stream.of("Nazwa","Kategoria","Ilość","Cena","Suma")
                .forEach(columnTitle ->{
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle));
                    header.setBackgroundColor(BaseColor.GREEN);
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
                    header.setVerticalAlignment(Element.ALIGN_CENTER);
                    table.addCell(header);

                });
    }

    private Font getFont(String type) {
        log.info("inside getFont");
        switch(type){
            case "Header":
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE ,18,BaseColor.BLACK);
                headerFont.setStyle(Font.BOLD);
                return headerFont;
            case "Data":
                Font dataFont = FontFactory.getFont(FontFactory.TIMES_ROMAN ,11,BaseColor.BLACK);
                dataFont.setStyle(Font.BOLD);
                return dataFont;
            default:
                return new Font();
        }
    }

    private void setRectangleInPdf(Document document) throws DocumentException {
        log.info("Inside setRectangleInPdf");
        Rectangle rec = new Rectangle(577,825,18,15);
        rec.enableBorderSide(1);
        rec.enableBorderSide(2);
        rec.enableBorderSide(4);
        rec.enableBorderSide(8);
        rec.setBackgroundColor(BaseColor.WHITE);
        rec.setBorderWidth(1);
        document.add(rec);

    }


    private void insertBill(Map<String, Object> requestMap) {
        try{
            Bill bill = new Bill();
            bill.setUuid((String) requestMap.get("uuid"));
            bill.setName((String) requestMap.get("name"));
            bill.setEmail((String)requestMap.get("email"));
            bill.setContactNumber((String) requestMap.get("contactNumber"));
            bill.setPaymentMethod((String) requestMap.get("paymentMethod"));
            bill.setTotal(Integer.parseInt((String) requestMap.get("totalAmount")));
            bill.setProductDetails((String) requestMap.get("productDetails"));
            bill.setCreatedBy(jwtFilter.getCurrentUser());
            billDao.save(bill);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private boolean validateRequestMap(Map<String, Object> requestMap) {
       return requestMap.containsKey("name") && requestMap.containsKey("contactNumber") &&
               requestMap.containsKey("email") && requestMap.containsKey("paymentMethod") &&
               requestMap.containsKey("productDetails") && requestMap.containsKey("totalAmount");
    }

    @Override
    public ResponseEntity<List<Bill>> getBills() {
      List<Bill> list = new ArrayList<>();
      if (jwtFilter.isAdmin()){
        list = billDao.getAllBills();
      }
      else {
          list = billDao.getBillByUserName(jwtFilter.getCurrentUser());
      }
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> deleteBill(Integer id) {
        try{
            Optional optional = billDao.findById(id);
            if(!optional.isEmpty()){
                billDao.deleteById(id);
                return CafeUtils.getResponseEntity("Rachunek pomyślnie usnięty",HttpStatus.OK);
            }
            return CafeUtils.getResponseEntity("Id rachunku nie istnieje",HttpStatus.OK);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
