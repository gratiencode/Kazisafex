/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import data.Presence;
import data.CompteTresor;
import data.finance.BilanReport;
import data.finance.CompteResultatReport;
import com.endeleya.kazisafex.MainuiController;
import com.endeleya.kazisafex.ProduitsController;
import tools.FileUtils;
import java.time.LocalTime;
import utilities.PDFUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import data.Entreprise;
import java.awt.Color;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Format;
import delegates.ProduitDelegate;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import tools.ComptageItem;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import data.BaseModel;
import data.Category;
import data.Client;
import data.Destocker;
import data.Fournisseur;
import javafx.stage.FileChooser;
import java.awt.Desktop;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import java.util.UUID;
import data.LigneVente;
import data.Livraison;
import data.Mesure;
import data.Operation;
import data.Periode;
import data.PrixDeVente;
import data.Produit;
import data.Recquisition;
import data.Stocker;
import data.Traisorerie;
import data.Vente;
import data.Immobilisation;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
//import com.microsoft.schemas.office.visio.x2012.main.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCreationHelper;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import data.helpers.Mouvment;
import data.PermitTo;
import data.helpers.TypeTraisorerie;
import delegates.StockerDelegate;
import delegates.PermissionDelegate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;
import services.PlatformUtil;
import utilities.ImageProduit;
import utilities.Peremption;
import utilities.Relevee;

/**
 *
 * @author eroot
 */
public class Util {

    static Preferences pref = Preferences.userNodeForPackage(SyncEngine.class);

    public static void sync(BaseModel bm, String act, Tables t) {
        String user = pref.get("userid", "na");
        bm.setFrom(user);
        bm.setType(t.name());
        syncModel(bm, act);
    }

    public static void sendText(String text) {
        SyncEndpoint endp = SyncEndpoint.getInstance();
        // endp.sendMessage(text);
    }

    public static String toPlain(double number) {
        return BigDecimal.valueOf(number).toPlainString();
    }

    public static void syncModel(final BaseModel bm, final String action) {
        SyncEndpoint endp = SyncEndpoint.getInstance();
        bm.setAction(action);
        bm.setType(bm.getType());
        bm.setPriority(0);
        bm.setCount(1);
        String user = pref.get("userid", "na");
        bm.setFrom(user);
        bm.setCounter(1);
        if (endp != null) {
            endp.sendObject(bm);
        }
    }

    public static void syncImage(ImageProduit img) {
        SyncEndpoint endp = SyncEndpoint.getInstance();
        if (endp != null) {
            endp.sendObject(img);
        }
    }

    public static void syncList(List objs) {
        SyncEndpoint endp = SyncEndpoint.getInstance();
        if (endp != null) {
            // endp.sendListObject(objs);
        }
    }

    public static void async(BaseModel bm, String action, Tables t) {
        SyncEndpoint endp = SyncEndpoint.getInstance();
        bm.setAction(action);
        bm.setType(t.name());
        String user = pref.get("userid", "na");
        bm.setFrom(user);
        bm.setCount(1);
        bm.setCounter(1);
        if (endp != null) {
            // endp.async(bm);
        }
    }

    public static void syncModel(final BaseModel bm, final String action, final long counter, final long count) {
        SyncEndpoint endp = SyncEndpoint.getInstance();
        bm.setAction(action);
        bm.setCount(count);
        String user = pref.get("userid", "na");
        bm.setFrom(user);
        bm.setCounter(counter);
        if (endp != null) {
            endp.sendObject(bm);
        }

    }

    public static Produit findProduit(Collection<Produit> lismez, String uid) {
        if (lismez != null) {
            for (Produit m : lismez) {
                if (m.getUid().equals(uid)) {
                    return m;
                }
            }
        }
        return null;
    }

    public static <T> T find(List<T> data, String uid) {
        for (T t : data) {
            if (t instanceof PrixDeVente) {
                PrixDeVente pv = (PrixDeVente) t;
                if (pv.getUid().equals(uid)) {
                    return t;
                }
            } else if (t instanceof Fournisseur) {
                Fournisseur f = (Fournisseur) t;
                if (f.getUid().equals(uid)) {
                    return t;
                }
            }
        }
        return null;
    }

    public static double currentBalance(List<Traisorerie> trs, TypeTraisorerie typeTresor, String devise) {
        double usd = 0, cdf = 0;
        for (Traisorerie tr : trs) {
            if (tr.getTypeTresorerie().equals(typeTresor.name())) {
                if (tr.getMouvement().equals(Mouvment.AUGMENTATION.name())) {
                    usd += tr.getMontantUsd();
                    cdf += tr.getMontantCdf();
                } else if (tr.getMouvement().equals(Mouvment.DIMINUTION.name())) {
                    usd -= tr.getMontantUsd();
                    cdf -= tr.getMontantCdf();
                }
            }
        }
        if (devise.equalsIgnoreCase("CDF")) {
            return cdf;
        } else {
            return usd;
        }
    }

    public static void setImageResourceOn(ImageView imgvu, String fileName) {
        InputStream is = MainuiController.class.getResourceAsStream("/icons/" + fileName);
        Image image = new Image(is);
        imgvu.setImage(image);
        Util.centerImage(imgvu);
    }

    private static ChartItem findItem(List<ChartItem> li, ChartItem x) {
        for (ChartItem ci : li) {
            if (ci.getSerieName().equals(x.getSerieName()) && ci.getAbsices().equals(x.getAbsices())) {
                return ci;
            }
        }
        return null;
    }

    public static Category findCategory(List<Category> lismez, String uid) {
        for (Category m : lismez) {
            if (m.getUid().equals(uid)) {
                return m;
            }
        }
        return null;
    }

    public static Category findCategoryDescr(List<Category> lismez, String desc) {
        for (Category m : lismez) {
            if (m.getDescritption().equalsIgnoreCase(desc)) {
                return m;
            }
        }
        return null;
    }

    public static <T> List<List<T>> partitions(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }

    public static <T> List<String> getMonths(List<T> ltr) {
        List<String> result = new ArrayList<>();
        for (T t : ltr) {
            if (t instanceof Vente) {
                Vente v = (Vente) t;
                int ann = v.getDateVente().getYear();
                int moi = v.getDateVente().getMonthValue();
                String mnf = ann + "-" + moi;
                if (!result.contains(mnf)) {
                    result.add(mnf);
                }
            } else if (t instanceof Operation) {
                Operation o = (Operation) t;
                int ann = o.getDate().getYear();
                int moi = o.getDate().getMonthValue();
                String mnf = ann + "-" + moi;
                if (!result.contains(mnf)) {
                    result.add(mnf);
                }
            } else if (t instanceof Traisorerie) {
                Traisorerie o = (Traisorerie) t;
                int ann = o.getDate().getYear();
                int moi = o.getDate().getMonthValue();
                String mnf = ann + "-" + moi;
                if (!result.contains(mnf)) {
                    result.add(mnf);
                }
            }

        }
        return result;
    }

    public static <T> List<String> getYears(List<T> ltr) {
        List<String> result = new ArrayList<>();
        for (T t : ltr) {
            if (t instanceof Vente) {
                Vente v = (Vente) t;
                int ann = v.getDateVente().getYear();
                String mnf = String.valueOf(ann);
                if (!result.contains(mnf)) {
                    result.add(mnf);
                }
            } else if (t instanceof Operation) {
                Operation o = (Operation) t;
                int ann = o.getDate().getYear();
                String mnf = String.valueOf(ann);
                if (!result.contains(mnf)) {
                    result.add(mnf);
                }
            } else if (t instanceof Traisorerie) {
                Traisorerie o = (Traisorerie) t;
                int ann = o.getDate().getYear();
                String mnf = String.valueOf(ann);
                if (!result.contains(mnf)) {
                    result.add(mnf);
                }
            }

        }
        return result;
    }

    public static LivraisonItem createItemFrom(List<Stocker> stocks) {
        LivraisonItem lvi = new LivraisonItem();
        double quant = 0;
        double sum = 0;
        for (Stocker stock : stocks) {
            lvi.setProduit(stock.getProductId());
            Mesure m = stock.getMesureId();
            quant += (stock.getQuantite() * m.getQuantContenu());
            sum += stock.getPrixAchatTotal();
            lvi.setMesure(m);
            lvi.setLivraison(stock.getLivraisId());
        }
        lvi.setSommeTotal(sum);
        lvi.setQuantite(quant);
        lvi.setLotcount(stocks.size());
        return lvi;
    }

    public static List<LivraisonItem> filterLivraisonItem(ObservableList<Stocker> stocks) {
        Collection<Produit> prox = findProduitInLivraison(stocks);
        List<LivraisonItem> rst = new ArrayList();
        for (Produit pro : prox) {
            double somQ = 0;
            LivraisonItem lvi = new LivraisonItem();
            lvi.setProduit(pro);
            List<Stocker> ss = findStockersForProduit(stocks, pro.getUid());
            lvi.setLotcount(ss.size());
            for (Stocker stock : stocks) {
                if (stock.getProductId().getUid().equals(pro.getUid())) {
                    lvi.setMesure(stock.getMesureId());
                    lvi.setLivraison(stock.getLivraisId());
                    somQ += stock.getQuantite();

                }
            }
            lvi.setQuantite(somQ);
            rst.add(lvi);
        }
        return rst;
    }

    public static <T> List<String> getDays(List<T> ltr) {
        List<String> result = new ArrayList<>();
        for (T t : ltr) {
            if (t instanceof Vente) {
                Vente v = (Vente) t;
                int ann = v.getDateVente().getYear();
                int moi = v.getDateVente().getMonthValue();
                int jrs = v.getDateVente().getDayOfMonth();
                String mnf = ann + "-" + moi + "-" + jrs;
                if (!result.contains(mnf)) {
                    result.add(mnf);
                }
            } else if (t instanceof Operation) {
                Operation o = (Operation) t;
                int ann = o.getDate().getYear();
                int moi = o.getDate().getMonthValue();
                int jrs = o.getDate().getDayOfMonth();
                String mnf = ann + "-" + moi + "-" + jrs;
                if (!result.contains(mnf)) {
                    result.add(mnf);
                }
            } else if (t instanceof Traisorerie) {
                Traisorerie o = (Traisorerie) t;
                int ann = o.getDate().getYear();
                int moi = o.getDate().getMonthValue();
                int jrs = o.getDate().getDayOfMonth();
                String mnf = ann + "-" + moi + "-" + jrs;
                if (!result.contains(mnf)) {
                    result.add(mnf);
                }
            }

        }
        return result;
    }

    /**
     *
     * @param <X>   type de donnee a filtree
     * @param lx    liste de donnee a filtrer
     * @param debut date du debut de filtre
     * @param fin   date de fin filtre
     * @return
     */
    public static <X> List<X> getDataBetween(List<X> lx, LocalDate debut, LocalDate fin) {
        List<X> result = new ArrayList<>();
        lx.forEach((x) -> {
            if (x instanceof Vente) {
                Vente vente = (Vente) x;
                if (isDateBetween(debut, fin, vente.getDateVente().toLocalDate())) {
                    result.add(x);
                }
            } else if (x instanceof Operation) {
                Operation oper = (Operation) x;
                if (isDateBetween(debut, fin, oper.getDate().toLocalDate())) {
                    result.add(x);
                }
            } else if (x instanceof Traisorerie) {
                Traisorerie trz = (Traisorerie) x;
                if (isDateBetween(debut, fin, trz.getDate().toLocalDate())) {
                    result.add(x);
                }
            }
        });
        return result;
    }

    public static <X> List<X> getDataBetween(List<X> lx, LocalDate debut, LocalDate fin, String region) {
        List<X> result = new ArrayList<>();
        for (X x : lx) {
            if (x instanceof Vente) {
                Vente vente = (Vente) x;
                if (vente.getRegion() == null) {
                    continue;
                }
                if (vente.getRegion().equals(region)) {
                    if (isDateBetween(debut, fin, vente.getDateVente().toLocalDate())) {
                        result.add(x);
                    }
                }
            } else if (x instanceof Operation) {
                Operation oper = (Operation) x;
                if (oper.getRegion() == null) {
                    continue;
                }
                if (oper.getRegion().equals(region)) {
                    if (isDateBetween(debut, fin, oper.getDate().toLocalDate())) {
                        result.add(x);
                    }
                }
            } else if (x instanceof Traisorerie) {
                Traisorerie trz = (Traisorerie) x;
                if (trz.getRegion() == null) {
                    continue;
                }
                if (trz.getRegion().equals(region)) {
                    if (isDateBetween(debut, fin, trz.getDate().toLocalDate())) {
                        result.add(x);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Cette function groupe les vente selon une critere autre que celles de
     * temps et retourne les ventes deja groupee
     *
     * @param <X>      Type de donee a traiter
     * @param db
     * @param ldata    liste triee de donnee soit vente, operation ou traisorerie
     * @param criteria critere de groupement detaille, par categorie ou par
     *
     * @param taux     taux de change actuel dollars franc
     * @return
     */
    // public static <X> List<List<ChartItem>> getGrouped(Kazisafe ksf, Entreprise
    // e, Nitrite db, List<X> ldata, String criteria, double taux) {
    // List<List<ChartItem>> result = new ArrayList<>();
    // List<ChartItem> preresult = new ArrayList<>();
    //
    // if (criteria.equalsIgnoreCase("Détaillé")) {
    // for (X x : ldata) {
    // if (x instanceof Vente) {
    // Vente vent = (Vente) x;
    // ChartItem chartItem = new ChartItem();
    // chartItem.setAbsices(Constants.DATE_HEURE_FORMAT.format(vent.getDateVente()));
    // chartItem.setAmmount(vent.getMontantUsd() + (vent.getMontantCdf() / taux) +
    // (vent.getMontantDette() == null ? 0 : vent.getMontantDette()));
    // chartItem.setDate(vent.getDateVente());
    // chartItem.setSerieName("Vente");
    // preresult.add(chartItem);
    // } else if (x instanceof Operation) {
    // Operation ops = (Operation) x;
    // ChartItem chartItem = new
    // ChartItem(Constants.DATE_HEURE_FORMAT.format(ops.getDate()), ops.getDate(),
    // ops.getMontantUsd() + (ops.getMontantCdf() / taux), "Depense");
    // preresult.add(chartItem);
    // } else if (x instanceof Traisorerie) {
    // Traisorerie trz = (Traisorerie) x;
    // ChartItem chartItem = new
    // ChartItem(Constants.DATE_HEURE_FORMAT.format(trz.getDate()), trz.getDate(),
    // trz.getMontantUsd() + (trz.getMontantCdf() / taux), "Depense+");
    // preresult.add(chartItem);
    // }
    // }
    // result.add(preresult);
    // return result;
    // }
    // switch (criteria) {
    // case "Par catégorie":
    // for (X x : ldata) {
    // if (x instanceof Vente) {
    // Vente vent = (Vente) x;
    // System.err.println(">>>>>> venete " + vent.getUid());
    // NitriteStorage<LigneVente> nslnvent = new NitriteStorage<>(e, ksf, db,
    // LigneVente.class);
    // List<LigneVente> lvs = vent.getLigneVenteList() != null ?
    // vent.getLigneVenteList()
    // : nslnvent.findAllEquals("reference", String.valueOf(vent.getUid()));
    // System.err.println("Ligne v " + lvs.size());
    // List<ChartItem> presult = new ArrayList<>();
    // for (LigneVente lv : lvs) {
    // Produit pr = lv.getProductId();
    // Category c = pr.getCategoryId();
    // ChartItem ci = new ChartItem();
    // double sumCat = 0;
    // for (LigneVente l : lvs) {
    // Produit p = l.getProductId();
    // Category cat = p.getCategoryId();
    // if (cat.getUid().equalsIgnoreCase(c.getUid())) {
    // sumCat += l.getMontantUsd();
    // }
    // }
    //
    // ci.setSerieName(c.getDescritption());
    // ci.setDate(vent.getDateVente());
    // ci.setAbsices(Constants.DATE_HEURE_FORMAT.format(vent.getDateVente()));
    // ci.setAmmount(sumCat);
    // if (findItem(presult, ci) == null) {
    // System.err.println(">>>>>>>. somme cat " + sumCat + " cat " +
    // c.getDescritption() + " " + ci.getAbsices());
    // presult.add(ci);
    // }
    // }
    // System.err.println("Presult size || = " + presult.size());
    // preresult.addAll(presult);
    // }
    // }
    // System.err.println("Preresult + " + preresult.size());
    // List<String> cats = filterCat(preresult);
    // for (String sc : cats) {
    // List<ChartItem> filted = filterSeries(preresult, sc);
    // result.add(filted);
    // }
    // break;
    // case "Par région":
    // List<String> regions = filter(ldata, 2);
    // for (String regs : regions) {
    // X i = ldata.get(0);
    // if (i instanceof Vente) {
    // List<Vente> v4reg = getForRegion(ldata, regs);
    // List<ChartItem> serie = new ArrayList<>();
    // for (Vente v : v4reg) {
    // ChartItem ci = new ChartItem();
    // ci.setAbsices(Constants.DATE_HEURE_FORMAT.format(v.getDateVente()));
    // double sum = v.getMontantUsd() + (v.getMontantDette() == null ? 0 :
    // v.getMontantDette()) + (v.getMontantCdf() / taux);
    // ci.setAmmount(sum);
    // ci.setDate(v.getDateVente());
    // ci.setSerieName(regs);
    // serie.add(ci);
    // }
    // result.add(serie);
    // } else if (i instanceof Operation) {
    //
    // List<Operation> opers = getForRegionOps(ldata, regs);
    // System.out.println("Opersize " + opers.size() + " reg = " + regs);
    // List<ChartItem> serie = new ArrayList<>();
    // for (Operation oper : opers) {
    // ChartItem ci = new ChartItem();
    // ci.setAbsices(Constants.DATE_HEURE_FORMAT.format(oper.getDate()));
    // ci.setAmmount(((oper.getMontantCdf() / taux) + oper.getMontantUsd()));
    // ci.setDate(oper.getDate());
    // ci.setSerieName(oper.getRegion());
    // serie.add(ci);
    // }
    // result.add(serie);
    // }
    //
    // }
    // break;
    // case "Par produit":
    // for (X x : ldata) {
    // if (x instanceof Vente) {
    // Vente vent = (Vente) x;
    // NitriteStorage<LigneVente> nslnv = new NitriteStorage<>(e, ksf, db,
    // LigneVente.class);
    // List<LigneVente> lvx = vent.getLigneVenteList() != null ?
    // vent.getLigneVenteList()
    // : nslnv.findAllEquals("reference", String.valueOf(vent.getUid()));
    // List<ChartItem> presultat = new ArrayList<>();
    // for (LigneVente lv : lvx) {
    // Produit pr = lv.getProductId();
    // ChartItem ci = new ChartItem();
    // double sumPro = 0;
    // for (LigneVente l : lvx) {
    // Produit p = l.getProductId();
    // if (p.getUid().equals(pr.getUid())) {
    // sumPro += l.getMontantUsd();
    // }
    // }
    // ci.setSerieName(pr.getMarque() + " " + pr.getModele() + " " + (pr.getTaille()
    // == null ? "" : pr.getTaille()));
    // ci.setDate(vent.getDateVente());
    // ci.setAbsices(Constants.DATE_HEURE_FORMAT.format(vent.getDateVente()));
    // ci.setAmmount(sumPro);
    // if (!presultat.contains(ci)) {
    // presultat.add(ci);
    // }
    // }
    // preresult.addAll(presultat);
    // }
    // }
    // System.err.println("Preresult + " + preresult.size());
    // List<String> catx = filterCat(preresult);
    // for (String sc : catx) {
    // List<ChartItem> filted = filterSeries(preresult, sc);
    // result.add(filted);
    // }
    // break;
    // case "Par fonction":
    // List<ChartItem> chis = toChartItems(ldata, taux);
    // List<String> lfunc = filter(chis, 1);
    // for (String function : lfunc) {
    // List<Operation> opers = getForDepartment(ldata, function);
    // List<ChartItem> serie = new ArrayList<>();
    // for (Operation oper : opers) {
    // ChartItem ci = new ChartItem();
    // ci.setAbsices(Constants.DATE_HEURE_FORMAT.format(oper.getDate()));
    // ci.setAmmount(((oper.getMontantCdf() / taux) + oper.getMontantUsd()));
    // ci.setDate(oper.getDate());
    // ci.setSerieName(oper.getImputation());
    // serie.add(ci);
    // }
    // result.add(serie);
    // }
    //
    // break;
    // }
    //
    // return result;
    // }
    /**
     * Cette fonction calcul le resultat se basant sur le prix de revient total
     * pour une entity
     *
     * @param <X>    type de donnee
     * @param db     source de donnee NO2
     * @param ldep   liste de depense
     * @param lvente liste de vente
     * @param region region
     * @param taux   taux de change
     * @return la liste de serie graphique a afficher
     */
    // public static <X, Y> List<List<ChartItem>> getGroupedForResult(Nitrite db,
    // List<X> ldep, List<Y> lvente, String region, double taux) {
    // List<List<ChartItem>> resultat = new ArrayList<>();
    // if (region == null) {
    // //PR tot
    // //vente progress
    //
    // List<ChartItem> serie = new ArrayList<>();
    // double depsum = 0;
    // for (X e : ldep) {
    // if (e instanceof Operation) {
    // Operation oper = (Operation) e;
    // ChartItem ci = new ChartItem();
    // ci.setAbsices(Constants.DATE_HEURE_FORMAT.format(oper.getDate()));
    // ci.setAmmount(((oper.getMontantCdf() / taux) + oper.getMontantUsd()));
    // ci.setDate(oper.getDate());
    // ci.setSerieName("Depenses");
    // depsum += ci.getAmmount();
    // serie.add(ci);
    // }
    // }
    //
    // System.out.println("SUM EXPENSES = " + depsum);
    // List<ChartItem> seriev = new ArrayList<>();
    // List<ChartItem> serier = new ArrayList<>();
    // double ventsum = 0;
    // for (Y e : lvente) {
    // if (e instanceof Vente) {
    // Vente v = (Vente) e;
    // ChartItem ci = new ChartItem();
    // ci.setAbsices(Constants.DATE_HEURE_FORMAT.format(v.getDateVente()));
    // double sum = v.getMontantUsd() + (v.getMontantDette() == null ? 0 :
    // v.getMontantDette()) + (v.getMontantCdf() / taux);
    // ci.setAmmount(sum);
    // ci.setDate(v.getDateVente());
    // ci.setSerieName("Vente");
    // seriev.add(ci);
    // ventsum += sum;
    // double rst = ventsum - depsum;
    // System.out.println("SUM result = " + ventsum + " - " + depsum + " = " + rst);
    // ChartItem cx = new ChartItem();
    // cx.setAbsices(Constants.DATE_HEURE_FORMAT.format(v.getDateVente()));
    // cx.setAmmount(rst);
    // cx.setDate(v.getDateVente());
    // cx.setSerieName("Marge");
    // serier.add(cx);
    // }
    // }
    // Collections.sort(serie, new ChartItem());
    // resultat.add(serie);
    // Collections.sort(serier, new ChartItem());
    // resultat.add(seriev);
    // resultat.add(serier);
    // return resultat;
    // }
    //
    // List<Operation> opers = getForRegionOps(ldep, region);
    // System.out.println("Opersize " + opers.size());
    // List<ChartItem> serie = new ArrayList<>();
    // double depsum = 0;
    // for (Operation oper : opers) {
    // ChartItem ci = new ChartItem();
    // ci.setAbsices(Constants.DATE_HEURE_FORMAT.format(oper.getDate()));
    // ci.setAmmount(((oper.getMontantCdf() / taux) + oper.getMontantUsd()));
    // ci.setDate(oper.getDate());
    // ci.setSerieName("Depenses");
    // depsum += ci.getAmmount();
    // serie.add(ci);
    // }
    // resultat.add(serie);
    // System.out.println("Montant depense = " + depsum);
    // List<Vente> v4reg = getForRegion(lvente, region);
    // List<ChartItem> seriev = new ArrayList<>();
    // List<ChartItem> serier = new ArrayList<>();
    // double ventsum = 0;
    // for (Vente v : v4reg) {
    // ChartItem ci = new ChartItem();
    // ci.setAbsices(Constants.DATE_HEURE_FORMAT.format(v.getDateVente()));
    // double sum = v.getMontantUsd() + (v.getMontantDette() == null ? 0 :
    // v.getMontantDette()) + (v.getMontantCdf() / taux);
    // ci.setAmmount(sum);
    // ci.setDate(v.getDateVente());
    // ci.setSerieName("Vente");
    // seriev.add(ci);
    // ventsum += sum;
    //
    // double rst = ventsum - depsum;
    //
    // ChartItem cx = new ChartItem();
    // cx.setAbsices(Constants.DATE_HEURE_FORMAT.format(v.getDateVente()));
    // cx.setAmmount(rst);
    // cx.setDate(v.getDateVente());
    // cx.setSerieName("Marge");
    // serier.add(cx);
    // }
    // for (Vente v : v4reg) {
    // double rst = ventsum - depsum;
    // System.out.println("SUM result = " + rst + " " + ventsum);
    // ChartItem cx = new ChartItem();
    // cx.setAbsices(Constants.DATE_HEURE_FORMAT.format(v.getDateVente()));
    // cx.setAmmount(rst);
    // cx.setDate(v.getDateVente());
    // cx.setSerieName("Marge");
    // serier.add(cx);
    //
    // }
    // resultat.add(seriev);
    // resultat.add(serier);
    // return resultat;
    // }
    private static List<String> filterCat(List<ChartItem> ci) {
        List<String> result = new ArrayList<>();
        for (ChartItem c : ci) {
            for (ChartItem cx : ci) {
                if (c.getSerieName().equalsIgnoreCase(cx.getSerieName())) {
                    if (!result.contains(c.getSerieName())) {
                        result.add(c.getSerieName());
                    }
                }
            }
        }
        return result;
    }

    public static String dbPath(String dbname) {
        String path, fpath = null;
        if (PlatformUtil.isWindows()) {
            path = System.getenv("ProgramData") + File.separator + "Kazisafe" + File.separator + "datastore";
            fpath = path + File.separator + dbname;
        } else if (PlatformUtil.isLinux()) {
            path = "/home/" + System.getProperty("user.name") + "/Kazisafe/datastore";
            fpath = path + File.separator + dbname;
        } else if (PlatformUtil.isMac()) {
            path = "/Users" + File.separator + System.getProperty("user.name") + File.separator + "Kazisafe"
                    + File.separator + "datastore";
            fpath = path + File.separator + dbname;
        }
        return fpath;
    }

    private static ChartItem findItemByAbsice(List<ChartItem> li, String x) {
        for (ChartItem ci : li) {
            if (ci.getAbsices().equalsIgnoreCase(x)) {
                return ci;
            }
        }
        return null;
    }

    private static List<ChartItem> filterSeries(List<ChartItem> lci, String desc) {
        List<ChartItem> result = new ArrayList<>();
        for (ChartItem ci : lci) {
            if (ci.getSerieName().equalsIgnoreCase(desc)) {
                result.add(ci);
            }
        }
        return result;
    }

    public static List<Operation> toOperation(List<Traisorerie> ltr) {
        List<Operation> result = new ArrayList<>();
        for (Traisorerie t : ltr) {
            if (t.getLibelle().equals("Approvisionnement") && t.getMouvement().equals(Mouvment.DIMINUTION.name())) {
                Operation op = new Operation();
                op.setCaisseOpId(t);
                op.setDate(t.getDate());
                op.setImputation(t.getLibelle());
                op.setLibelle(t.getLibelle());
                op.setMontantCdf(t.getMontantCdf());
                op.setMontantUsd(t.getMontantUsd());
                op.setMouvement(Mouvment.AUGMENTATION.name());
                op.setObservation(t.getLibelle());
                op.setRegion(t.getRegion());
                result.add(op);
            }
        }
        return result;
    }

    public static <T> List<Operation> getForDepartment(List<T> lops, String dep) {
        List<Operation> result = new ArrayList<>();
        for (T t : lops) {
            if (t instanceof Operation) {
                Operation o = (Operation) t;
                if (o.getImputation().equalsIgnoreCase(dep)) {
                    result.add(o);
                }
            }
        }
        return result;
    }

    public static <T> List<Operation> getForRegionOps(List<T> lops, String reg) {
        List<Operation> result = new ArrayList<>();
        for (T t : lops) {
            if (t instanceof Operation) {
                Operation o = (Operation) t;
                if (o.getRegion() != null) {
                    if (o.getRegion().equalsIgnoreCase(reg)) {
                        result.add(o);
                    }
                }
            }
        }
        return result;
    }

    public static <T> List<T> filterObject(List<T> lops, String uid) {
        List<T> result = new ArrayList<>();
        for (T t : lops) {
            try {
                Class cls = t.getClass();
                Object inst = cls.newInstance();
                Method method = cls.getMethod("uid");
                Object invoked = method.invoke(inst);
                if (String.valueOf(invoked).equals(uid)) {
                    result.add(t);
                }
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }

    public static <T> List<Vente> getForRegion(List<T> lops, String dep) {
        List<Vente> result = new ArrayList<>();
        for (T t : lops) {
            if (t instanceof Vente) {
                Vente o = (Vente) t;
                if (o.getRegion() != null) {
                    if (o.getRegion().equalsIgnoreCase(dep)) {
                        result.add(o);
                    }
                }
            }
        }
        return result;
    }

    public static <T> List<ChartItem> toChartItems(List<T> lops, double tx) {
        List<ChartItem> result = new ArrayList<>();
        for (T t : lops) {
            if (t instanceof Operation) {
                Operation o = (Operation) t;
                ChartItem ci = new ChartItem();
                ci.setAbsices(Constants.DATE_HEURE_FORMAT.format(o.getDate()));
                ci.setAmmount((o.getMontantCdf() / tx) + o.getMontantUsd());
                ci.setDate(o.getDate().toLocalDate());
                ci.setSerieName(o.getImputation());
                result.add(ci);
            }
        }
        Collections.sort(result, new ChartItem());
        return result;
    }

    public static List<LigneVente> filterVenteItems(List<LigneVente> ls, String prodId) {
        List<LigneVente> result = new ArrayList<>();
        ls.stream().filter((l) -> (l.getProductId().getUid().equals(prodId))).forEachOrdered((l) -> {
            result.add(l);
        });
        return result;
    }

    public static <T> List<String> filter(List<T> lops, int spec) {
        List<String> result = new ArrayList<>();
        for (T t : lops) {
            if (t instanceof Operation) {
                Operation o = (Operation) t;
                for (T x : lops) {
                    if (x instanceof Operation) {
                        Operation op = (Operation) t;
                        if (spec == 1) {
                            if (o.getImputation().equalsIgnoreCase(op.getImputation())) {
                                if (!result.contains(o.getImputation())) {
                                    result.add(o.getImputation());
                                }
                            }
                        } else if (spec == 2) {
                            if (o.getRegion() != null) {
                                if (o.getRegion().equalsIgnoreCase(op.getRegion())) {
                                    if (!result.contains(o.getRegion())) {
                                        result.add(o.getRegion());
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (t instanceof ChartItem) {
                ChartItem ci = (ChartItem) t;
                for (T f : lops) {
                    if (f instanceof ChartItem) {
                        ChartItem cis = (ChartItem) f;
                        if (ci.getSerieName().equalsIgnoreCase(cis.getSerieName())) {
                            if (!result.contains(ci.getSerieName())) {
                                result.add(ci.getSerieName());
                            }
                        }
                    }
                }
            } else if (t instanceof Vente) {
                Vente v = (Vente) t;
                for (T l : lops) {
                    if (l instanceof Vente) {
                        Vente vent = (Vente) l;
                        if (v.getRegion() != null) {
                            if (v.getRegion().equalsIgnoreCase(vent.getRegion())) {
                                if (!result.contains(v.getRegion())) {
                                    result.add(v.getRegion() == null ? " " : v.getRegion());
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public static List<LigneVente> findSaleItemsFor(List<LigneVente> items, Integer vent) {
        List<LigneVente> result = new ArrayList<>();
        for (LigneVente l : items) {
            if (l.getReference().getUid() == vent) {
                if (!result.contains(l)) {
                    result.add(l);
                }
            }
        }
        return result;
    }

    public static List<String> extractDates(List<Vente> lvs) {
        List<String> datex = new ArrayList<>();
        for (Vente v : lvs) {
            String date = v.getDateVente().toLocalDate().toString();
            if (!datex.contains(date)) {
                datex.add(date);
            }
        }
        return datex;
    }

    public static List<LocalDate> extractDates(List<Vente> lvs, String region) {
        List<LocalDate> datex = new ArrayList<>();
        for (Vente v : lvs) {
            if (v.getRegion() == null) {
                continue;
            }
            if (region == null) {
                LocalDate date = v.getDateVente().toLocalDate();
                if (!datex.contains(date)) {
                    datex.add(date);
                }
            } else {
                LocalDate date = v.getDateVente().toLocalDate();
                if (!datex.contains(date) && v.getRegion().equals(region)) {
                    datex.add(date);
                }
            }

        }
        return datex;
    }

    public static List<ChartItem> getTimeGrouped(List<ChartItem> lci, String timeCriteria) {
        List<ChartItem> result = new ArrayList<>();
        if (timeCriteria == null) {
            return lci;
        }
        switch (timeCriteria) {
            case "Par jours":
                System.out.println("lci list " + lci.size());
                for (ChartItem item : lci) {
                    String day = Constants.DATE_ONLY_FORMAT.format(item.getDate());
                    ChartItem dailly = getDailly(lci, day);
                    if (findItemByAbsice(result, dailly.getAbsices()) == null) {
                        result.add(dailly);
                    }
                }
                return result;
            case "Par mois":
                for (ChartItem item : lci) {
                    String month = Constants.YEAR_AND_MONTH_FORMAT.format(item.getDate());
                    ChartItem monthly = getMonthly(lci, month);
                    if (findItemByAbsice(result, monthly.getAbsices()) == null) {
                        result.add(monthly);
                    }
                }
                return result;
            case "Par année":
                for (ChartItem item : lci) {
                    String year = Constants.YEAR_ONLY_FORMAT.format(item.getDate());
                    ChartItem yearly = getAnnually(lci, year);
                    if (findItemByAbsice(result, yearly.getAbsices()) == null) {
                        result.add(yearly);
                    }
                }
                return result;
            default:
                return lci;
        }

    }

    public static ChartItem getDailly(List<ChartItem> lci, String date) {
        ChartItem ci = new ChartItem();
        double sum = 0;
        for (ChartItem objs : lci) {
            String day = Constants.DATE_ONLY_FORMAT.format(objs.getDate());
            if (day.equals(date)) {
                ci.setAbsices(day);
                ci.setDate(objs.getDate());
                sum += objs.getAmmount();
            }
            ci.setSerieName(objs.getSerieName());
        }
        ci.setAmmount(sum);
        return ci;
    }

    public static ChartItem getMonthly(List<ChartItem> lci, String mois) {
        ChartItem ci = new ChartItem();
        double sum = 0;
        for (ChartItem objs : lci) {
            String month = Constants.YEAR_AND_MONTH_FORMAT.format(objs.getDate());
            if (month.equals(mois)) {
                ci.setDate(objs.getDate());
                ci.setAbsices(month);
                sum += objs.getAmmount();
            }
            ci.setSerieName(objs.getSerieName());
        }
        ci.setAmmount(sum);
        return ci;
    }

    public static ChartItem getAnnually(List<ChartItem> lci, String annee) {
        ChartItem ci = new ChartItem();
        double sum = 0;
        for (ChartItem objs : lci) {
            String year = Constants.YEAR_ONLY_FORMAT.format(objs.getDate());
            if (year.equals(annee)) {
                ci.setDate(objs.getDate());
                ci.setAbsices(year);
                sum += objs.getAmmount();
            }
            ci.setSerieName(objs.getSerieName());
        }
        ci.setAmmount(sum);
        return ci;
    }

    public static Quintuplet<LocalDate, List<SaleItem>, Double, Double, Double> findByDate(List<SaleItem> lsi,
            LocalDate date) {
        Quintuplet<LocalDate, List<SaleItem>, Double, Double, Double> quint = new Quintuplet<>();
        List<SaleItem> ventesDuJour = new ArrayList<>();
        double montantusd = 0, montantcdf = 0, dette = 0;
        for (SaleItem si : lsi) {
            if (si.getDateDeVente() == null) {
                continue;
            }
            LocalDate d = si.getDateDeVente();
            if (date.toString().contains(d.toString())) {
                if (!ventesDuJour.contains(si)) {
                    montantusd += si.getSaleAmountUsd();
                    montantcdf += si.getSaleAmountCdf();
                    dette += si.getSaleAmountCredit();
                    ventesDuJour.add(si);
                }
            }
        }
        try {
            quint.setV(date);
        } catch (Exception e) {
            try {
                quint.setV(date);
            } catch (Exception ex) {
            }
        }
        quint.setW(ventesDuJour);
        quint.setX(montantusd);
        quint.setY(montantcdf);
        quint.setZ(dette);
        return quint;
    }

    public static Recquisition findLastRecquisitionFor(List<Recquisition> lr, String prodId) {
        List<Recquisition> entrees = Util.findRequisitionForProduit(lr, prodId);
        RequestHelper rh = Util.findRecent(entrees);
        if (rh == null) {
            return null;
        }
        Recquisition last = findRecqById(lr, rh.getRequid());
        return last;
    }

    public static boolean isDateBetween(LocalDate date1, LocalDate date2, LocalDate compared) {
        // date1<=compared && date2>=compared
        long datemin = date1.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long datemax = date2.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long comp = compared.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return datemin <= comp && datemax >= comp;
    }

    public static Produit findProduitByCodebar(List<Produit> lismez, String codebar) {
        for (Produit m : lismez) {
            if (m.getCodebar().equals(codebar)) {
                return m;
            }
        }
        return null;
    }

    public static List<PrixDeVente> findPricesForReq(List<PrixDeVente> pvs, String reqUid) {
        List<PrixDeVente> rst = new ArrayList<>();
        for (PrixDeVente pv : pvs) {
            if (pv.getRecquisitionId().equals(reqUid)) {
                rst.add(pv);
            }
        }
        return rst;
    }

    public static <T> List<T> checkAndFixIntegrity(List<T> list, List... ts) {
        List<T> result = new ArrayList<>();
        for (T t : list) {
            if (t instanceof Recquisition) {
                Recquisition recqusisition = (Recquisition) t;
                if (recqusisition.getMesureId() != null) {
                    result.add(t);
                }
            } else if (t instanceof Stocker) {
                Stocker stocker = (Stocker) t;
                if (stocker.getMesureId() != null) {
                    result.add(t);
                }
            } else if (t instanceof Destocker) {
                Destocker destocker = (Destocker) t;
                if (destocker.getMesureId() != null) {
                    result.add(t);
                }
            } else if (t instanceof PrixDeVente) {
                PrixDeVente prixDeVente = (PrixDeVente) t;
                if (prixDeVente.getMesureId() != null) {
                    result.add(t);
                }
            } else if (t instanceof LigneVente) {
                LigneVente ligneVente = (LigneVente) t;
                if (ligneVente.getMesureId() != null) {
                    result.add(t);
                }
            }
        }
        return result;
    }

    public static <T> List<T> filterNoNullMesure(List<T> list) {
        List<T> result = new ArrayList<>();
        for (T t : list) {
            if (t instanceof Recquisition) {
                Recquisition recqusisition = (Recquisition) t;
                if (recqusisition.getMesureId() != null) {
                    result.add(t);
                }
            } else if (t instanceof Stocker) {
                Stocker stocker = (Stocker) t;
                if (stocker.getMesureId() != null) {
                    result.add(t);
                }
            } else if (t instanceof Destocker) {
                Destocker destocker = (Destocker) t;
                if (destocker.getMesureId() != null) {
                    result.add(t);
                }
            } else if (t instanceof PrixDeVente) {
                PrixDeVente prixDeVente = (PrixDeVente) t;
                if (prixDeVente.getMesureId() != null) {
                    result.add(t);
                }
            } else if (t instanceof LigneVente) {
                LigneVente ligneVente = (LigneVente) t;
                if (ligneVente.getMesureId() != null) {
                    result.add(t);
                }
            }
        }
        return result.isEmpty() ? list : result;
    }

    public static List<PhysicalInventoryLine> importInventoryFromExcelFile(File f, String region)
            throws IllegalStateException {
        List<PhysicalInventoryLine> result = new ArrayList<>();
        try {
            FileInputStream fis = new FileInputStream(f);
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            XSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            StringBuilder sb = new StringBuilder();
            String regIn = null;
            rowsloop: while (rowIterator.hasNext()) {
                Row r1 = rowIterator.next();
                if (isRowEmpty(r1)) {
                    continue;
                }
                if (r1.getRowNum() == 2) {
                    Cell regioncell = r1.getCell(1);
                    regIn = regioncell.getStringCellValue();
                }
                if (r1.getRowNum() > 11) {
                    if (isRowEmpty(r1)) {
                        break;
                    }
                    PhysicalInventoryLine inventoryItem = new PhysicalInventoryLine();
                    inventoryItem.setRegion(regIn == null ? region : regIn);
                    inventoryItem.setLigne(r1.getRowNum() + 1);
                    Iterator<Cell> cellIterator = r1.cellIterator();
                    System.out.println("Ligne " + r1.getRowNum());
                    while (cellIterator.hasNext()) {
                        Cell m = cellIterator.next();
                        switch (m.getColumnIndex()) {
                            case 0 -> {
                                if (!isCellEmpty(m)) {
                                    if (!m.getCellType().equals(CellType.STRING)) {
                                        MainUI.notify(null, "Erreur",
                                                "La collone codebar doit etre en texte a la ligne "
                                                        + (m.getRowIndex() + 1),
                                                8, "error");
                                        return null;
                                    }
                                    String codebar = m.getStringCellValue();
                                    inventoryItem.setCodebarr(codebar);
                                }
                            }
                            case 1 -> {
                                if (!isCellEmpty(m)) {
                                    if (!m.getCellType().equals(CellType.STRING)) {
                                        MainUI.notify(null, "Erreur", "La nom du produit doit etre en texte a la ligne "
                                                + (m.getRowIndex() + 1), 8, "error");
                                        return null;
                                    }
                                    inventoryItem.setNomProduit(m.getStringCellValue());
                                }
                            }
                            case 2 -> {
                                if (!isCellEmpty(m)) {
                                    if (!m.getCellType().equals(CellType.STRING)) {
                                        MainUI.notify(null, "Erreur",
                                                "La collone marque ou fabriquant doit etre en texte a la ligne "
                                                        + (m.getRowIndex() + 1),
                                                8, "error");
                                        return null;
                                    }
                                    inventoryItem.setMarqueProduit(m.getStringCellValue());
                                }
                            }
                            case 3 -> {
                                if (!isCellEmpty(m)) {
                                    if (!m.getCellType().equals(CellType.STRING)) {
                                        MainUI.notify(null, "Erreur",
                                                "La collone modele ou forme doit etre en texte a la ligne "
                                                        + (m.getRowIndex() + 1),
                                                8, "error");
                                        return null;
                                    }
                                    inventoryItem.setModeleProduit(m.getStringCellValue());
                                }
                            }
                            case 4 -> {
                                if (!isCellEmpty(m)) {
                                    if (!m.getCellType().equals(CellType.STRING)) {
                                        MainUI.notify(null, "Erreur",
                                                "La collone taille ou concentration doit etre en texte a la ligne "
                                                        + (m.getRowIndex() + 1),
                                                8, "error");
                                        return null;
                                    }
                                    inventoryItem.setTailleProduit(m.getStringCellValue());
                                }
                            }
                            case 5 -> {
                                if (m.getCellType().equals(CellType.STRING)) {
                                    inventoryItem.setNumlot(m.getStringCellValue());
                                }
                            }
                            case 6 -> {
                                if (m.getCellType().equals(CellType.STRING)) {
                                    inventoryItem.setMesure(m.getStringCellValue());
                                }
                            }
                            case 7 -> {
                                if (m.getCellType().equals(CellType.NUMERIC)) {
                                    inventoryItem.setEntrees(m.getNumericCellValue());
                                }
                            }
                            case 8 -> {
                                if (m.getCellType().equals(CellType.NUMERIC)) {
                                    inventoryItem.setSorties(m.getNumericCellValue());
                                }
                            }
                            case 9 -> {
                                if (!isCellEmpty(m)) {
                                    if (m.getCellType().equals(CellType.NUMERIC)) {
                                        inventoryItem.setStockTheorique(m.getNumericCellValue());
                                    }
                                }
                            }
                            case 10 -> {
                                if (!isCellEmpty(m)) {
                                    if (m.getCellType().equals(CellType.NUMERIC)) {
                                        inventoryItem.setStockPhysique(m.getNumericCellValue());
                                    } else {
                                        MainUI.notify(null, "Erreur",
                                                "Le stock physique doit etre en numerique a la ligne "
                                                        + (m.getRowIndex() + 1),
                                                8, "error");
                                        return null;
                                    }
                                } else {
                                    MainUI.notify(null, "Erreur", "Le stock physique ne doit pas etre vide a la ligne "
                                            + (m.getRowIndex() + 1), 8, "error");
                                    return null;
                                }
                            }
                            case 11 -> {
                                // alert
                                if (!isCellEmpty(m)) {
                                    Double d = m.getNumericCellValue();
                                    inventoryItem.setStockAlerte(d == null ? 0 : d);
                                }
                            }
                            case 12 -> {
                                if (!isCellEmpty(m)) {
                                    if (m.getCellType().equals(CellType.NUMERIC)) {
                                        inventoryItem.setCoutAchat(m.getNumericCellValue());
                                    } else {
                                        MainUI.notify(null, "Erreur",
                                                "Le cout d'achat doit etre en numerique a la ligne "
                                                        + (m.getRowIndex() + 1),
                                                8, "error");
                                        return null;
                                    }
                                }
                            }
                            case 14 -> {
                                if (!isCellEmpty(m)) {
                                    inventoryItem.setLocalisation(m.getStringCellValue());
                                } else {
                                    inventoryItem.setLocalisation(region);
                                }
                            }
                            case 15 -> {
                                if (m.getCellType().equals(CellType.NUMERIC)) {
                                    LocalDate datexp = m.getLocalDateTimeCellValue().toLocalDate();
                                    if (datexp != null) {
                                        inventoryItem.setDateExpiration(datexp);
                                    }
                                }
                            }
                            case 16 -> {
                                if (!isCellEmpty(m)) {
                                    if (m.getCellType().equals(CellType.STRING)) {
                                        inventoryItem.setPrixDeVente(m.getStringCellValue());
                                    } else {
                                        MainUI.notify(null, "Erreur", "Le prix de vente a la ligne "
                                                + (m.getRowIndex() + 1)
                                                + " doit etre un nombre en format text sous la notation Kazisafe", 5,
                                                "error");
                                        return null;
                                    }
                                }
                            }
                            case 17 -> {
                                if (!isCellEmpty(m)) {
                                    if (m.getCellType().equals(CellType.STRING)) {
                                        inventoryItem.setDevise(m.getStringCellValue());
                                    } else {
                                        MainUI.notify(null, "Erreur",
                                                "L'indication de la devise est erronee a la ligne "
                                                        + (m.getRowIndex() + 1)
                                                        + " et doit etre en format text USD/CDF ",
                                                8, "error");
                                        return null;
                                    }
                                } else {
                                    MainUI.notify(null, "Erreur",
                                            "L'indication multibatch ne doit pas etre null a la ligne "
                                                    + (m.getRowIndex() + 1)
                                                    + " et doit etre en format text de valeur OUI ou NON ",
                                            8, "error");
                                    return null;
                                }
                            }
                            case 18 -> {
                                if (!isCellEmpty(m)) {
                                    if (m.getCellType().equals(CellType.STRING)) {
                                        inventoryItem.setMultiBatch(m.getStringCellValue().equals("OUI"));
                                    } else {
                                        MainUI.notify(null, "Erreur",
                                                "L'indication multibatch ne doit pas etre null a la ligne "
                                                        + (m.getRowIndex() + 1)
                                                        + " et doit etre en format text de valeur OUI ou NON ",
                                                8, "error");
                                        return null;
                                    }
                                } else {
                                    MainUI.notify(null, "Erreur",
                                            "L'indication multibatch ne doit pas etre null a la ligne "
                                                    + (m.getRowIndex() + 1)
                                                    + " et doit etre en format text de valeur OUI ou NON ",
                                            8, "error");
                                    return null;
                                }
                            }
                            default -> {
                            }
                        }
                    }
                    System.err.println("IMPORT INV " + (r1.getRowNum() + 1));
                    result.add(inventoryItem);
                }
            }
            if (sb.length() > 0) {
                MainUI.notify(null, "Avertissement", sb.toString(), 8, "warn");
            }
            return result;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static List<DataImporter> importFromExcel(File f, List<Produit> lprods, String region, boolean conv)
            throws IllegalStateException {
        List<DataImporter> result = new ArrayList<>();
        try {
            FileInputStream fis = new FileInputStream(f);
            HSSFWorkbook workbook = new HSSFWorkbook(fis);
            HSSFSheet sheet = workbook.getSheetAt(0);
            if (!sheet.getSheetName().equals("Detaillant")) {
                MainUI.notify(null, "Erreur", "La premiere feuile du fichier doit porter ne nom [Detaillant]", 8,
                        "error");
                return null;
            }
            Iterator<Row> rowIterator = sheet.iterator();
            System.out.println("Importation en cours....");
            boolean doublon = false;
            rowsloop: while (rowIterator.hasNext()) {

                Row r1 = rowIterator.next();
                if (isRowEmpty(r1)) {
                    System.out.println(" cRNUM " + r1.getRowNum());
                    continue;
                }
                if (r1.getRowNum() > 0) {

                    Iterator<Cell> cellIterator = r1.cellIterator();
                    DataImporter data = new DataImporter();
                    Produit produit = new Produit(DataId.generate());
                    Stocker stock = new Stocker(DataId.generate());
                    stock.setDateStocker(LocalDateTime.now());
                    stock.setRegion(region);
                    Destocker destock = new Destocker(DataId.generate());
                    destock.setDateDestockage(LocalDateTime.now());
                    destock.setRegion(region);
                    Recquisition recq = new Recquisition(DataId.generate());
                    recq.setDate(LocalDateTime.now());
                    recq.setRegion(region);
                    PrixDeVente price = new PrixDeVente(DataId.generate());
                    // InputStream is =
                    // MainuiController.class.getResourceAsStream("/icons/gallery.png");
                    // byte[] img = FileUtils.readAllBytes(is);
                    // //produit.setImage(img);
                    System.out.println("Rowing:::" + r1.getRowNum());
                    produit.setDateCreation(LocalDateTime.now());
                    while (cellIterator.hasNext()) {
                        Cell m = cellIterator.next();
                        switch (m.getColumnIndex()) {
                            case 0:
                                String codebar = m.getStringCellValue();
                                // if(codebar==null){
                                // continue rowsloop;
                                // }
                                produit.setCodebar(conv ? Util.numfyFrenchChars(codebar) : codebar);
                                Produit prox = Util.findProduitByCodebar(lprods, codebar);
                                if (prox != null) {
                                    produit.setUid(prox.getUid());
                                }
                                break;
                            case 1:
                                produit.setNomProduit(m.getStringCellValue());
                                break;
                            case 2:
                                produit.setMarque(m.getStringCellValue());
                                break;
                            case 3:
                                produit.setModele(m.getStringCellValue());
                                break;
                            case 4:
                                produit.setTaille(m.getStringCellValue());
                                break;
                            case 5:
                                produit.setCouleur(m.getStringCellValue());
                                break;
                            case 6:
                                stock.setQuantite(m.getNumericCellValue());
                                destock.setQuantite(m.getNumericCellValue());
                                recq.setQuantite(m.getNumericCellValue());
                                break;
                            case 7:
                                stock.setStockAlerte(m.getNumericCellValue());
                                recq.setStockAlert(m.getNumericCellValue());
                                break;
                            case 8:
                                stock.setLocalisation(m.getStringCellValue());
                                break;
                            case 9:
                                stock.setCoutAchat(m.getNumericCellValue());
                                destock.setCoutAchat(m.getNumericCellValue());
                                recq.setCoutAchat(m.getNumericCellValue());
                                break;
                            case 10:

                                price.setPrixUnitaire(m.getNumericCellValue());
                                break;
                            case 11:
                                Date datexp = m.getDateCellValue();
                                if (datexp != null) {
                                    stock.setDateExpir(Constants.Datetime.toLocalDate(datexp));
                                    recq.setDateExpiry(Constants.Datetime.toLocalDate(datexp));
                                } else {
                                    stock.setDateExpir(null);
                                    recq.setDateExpiry(null);
                                }
                                break;

                        }
                    }

                    stock.setPrixAchatTotal(stock.getCoutAchat() * stock.getQuantite());
                    stock.setProductId(produit);
                    stock.setObservation("");
                    destock.setDestination(region);
                    destock.setObservation("");
                    destock.setProductId(produit);
                    recq.setObservation("Pret a la vente");
                    recq.setProductId(produit);
                    recq.setObservation("");
                    price.setDevise("USD");
                    price.setQmax(recq.getQuantite());
                    price.setQmin(0.01);
                    price.setRecquisitionId(recq);
                    data.setDestockage(destock);
                    data.setProduct(produit);
                    data.setRecquisition(recq);
                    data.setSalePrice(price);
                    data.setStock(stock);
                    System.err.println("IMPORT stock.pro = " + data.getStock().getProductId() + ""
                            + " destock.pro : " + data.getDestockage().getProductId() + " "
                            + " Recquis.pro : " + data.getRecquisition().getProductId() + " PODUCIT : "
                            + data.getProduct());
                    result.add(data);
                }
            }
            if (doublon) {
                doublon = false;
                MainUI.notify(null, "Doublon !", "Certains codebar existent déjà et n'ont pas pû être importés", 5,
                        "warn");
            }
            return result;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static LigneImport proceedImport(Row row, Produit produit, List<Produit> lprods, boolean conv,
            String region, String ref, String meth) {

        Stocker stocker = new Stocker(DataId.generate());
        Recquisition recq = new Recquisition(DataId.generate());
        Destocker destock = new Destocker(DataId.generate());
        Mesure mesure = new Mesure(DataId.generate());
        LigneImport importer = new LigneImport();
        produit.setDateCreation(LocalDateTime.now());
        Iterator<Cell> cellIterator = row.cellIterator();
        productloop: while (cellIterator.hasNext()) {
            Cell cellule = cellIterator.next();
            int index = cellule.getColumnIndex();

            if (isCellEmpty(cellule)) {

                continue;
            }
            switch (index) {
                case 0:
                    try {
                        if (cellule.getCellType().equals(CellType.STRING)) {
                            String m1 = cellule.getStringCellValue();
                            if (!isCellEmpty(cellule)) {
                                String rst = Util.numfyFrenchChars(m1);
                                produit.setCodebar(conv ? rst : m1);
                                Produit prox = Util.findProduitByCodebar(lprods, produit.getCodebar());
                                if (prox != null) {
                                    produit.setUid(prox.getUid());
                                }
                            } else {
                                MainUI.notify(null, "Erreur",
                                        "Le codebar est obligatoire, pour la ligne " + (row.getRowNum() + 1), 7,
                                        "error");
                                return null;

                            }
                        }
                    } catch (IllegalStateException e) {
                        MainUI.notify(null, "Erreur ", "Erreur " + e.getMessage() + " sur collone codebarr", 5,
                                "error");
                    }
                    break;
                case 1:
                    try {
                        String m2 = cellule.getStringCellValue();
                        if (!isCellEmpty(cellule)) {
                            produit.setNomProduit(m2);
                        } else {
                            MainUI.notify(null, "Erreur",
                                    "La nom du produit est obligatoire, pour la ligne " + (row.getRowNum() + 1), 7,
                                    "error");
                            return null;

                        }
                    } catch (IllegalStateException e) {
                        MainUI.notify(null, "Erreur ", "Erreur " + e.getMessage() + " sur collone nom du produit", 5,
                                "error");
                    }
                    break;
                case 2:
                    try {
                        String m3 = cellule.getStringCellValue();
                        if (!isCellEmpty(cellule)) {
                            produit.setMarque(m3);
                        }
                    } catch (IllegalStateException e) {
                        MainUI.notify(null, "Erreur ", "Erreur " + e.getMessage() + " sur collone marque", 5, "error");
                    }
                    break;
                case 3:
                    try {
                        String m4 = cellule.getStringCellValue();
                        if (!isCellEmpty(cellule)) {
                            produit.setModele(m4);
                        }
                    } catch (IllegalStateException e) {
                        MainUI.notify(null, "Erreur ", "Erreur " + e.getMessage() + " sur collone modele", 5, "error");
                    }

                    break;
                case 4:
                    try {
                        String m5 = cellule.getStringCellValue();
                        if (!isCellEmpty(cellule)) {
                            produit.setTaille(m5);
                        }
                    } catch (IllegalStateException e) {
                        MainUI.notify(null, "Erreur ", "Erreur " + e.getMessage() + " sur collone taille/concentration",
                                5, "error");
                    }
                    break;
                case 5:
                    try {
                        String m6 = cellule.getStringCellValue();
                        if (!isCellEmpty(cellule)) {
                            produit.setCouleur(m6);
                        }
                    } catch (IllegalStateException e) {
                        MainUI.notify(null, "Erreur ", "Erreur " + e.getMessage() + " sur collone couleur", 5, "error");
                    }
                    break;
                case 6:
                    if (!isCellEmpty(cellule)) {
                        if (cellule.getCellType().equals(CellType.STRING)) {
                            try {
                                String value = cellule.getStringCellValue();
                                stocker.setNumlot(value);
                                recq.setNumlot(value);
                                destock.setNumlot(value);
                            } catch (Exception e) {
                                MainUI.notify(null, "Error", "Erreur num-lot " + e.getMessage(), 5, "error");
                            }
                        }
                    } else {
                        try {
                            String value = Constants.TIMESTAMPED_FORMAT.format(new Date());
                            stocker.setNumlot(value);
                            recq.setNumlot(value);
                            destock.setNumlot(value);
                        } catch (Exception e) {
                            MainUI.notify(null, "Error", "Erreur num-lot " + e.getMessage(), 5, "error");
                        }
                    }
                    break;
                case 7:
                    if (!isCellEmpty(cellule)) {
                        try {
                            String m = cellule.getStringCellValue();
                            String mzc[] = m.split(":");
                            mesure.setProduitId(produit);
                            mesure.setDescription(mzc[0]);
                            System.out.println("Mezimport " + mzc[0] + " " + produit);
                            if (mzc.length < 2) {
                                MainUI.notify(null, "Erreur", "La notation de la mesure est incomplete, pour la ligne "
                                        + (row.getRowNum() + 1), 7, "error");
                                return null;
                            }
                            mesure.setQuantContenu(Double.parseDouble(mzc[1]));
                            stocker.setMesureId(mesure);
                            recq.setMesureId(mesure);
                            destock.setMesureId(mesure);
                        } catch (Exception e) {
                            MainUI.notify(null, "Error", "Erreur mesure " + e.getMessage(), 5, "error");
                        }
                    } else {
                        MainUI.notify(null, "Erreur",
                                "Le mesurage d'unite est toujours obligatoire, il est absent a la ligne "
                                        + (row.getRowNum() + 1),
                                7, "error");
                        return null;
                    }
                    break;
                case 8:
                    if (!isCellEmpty(cellule)) {
                        if (cellule.getCellType().equals(CellType.NUMERIC)) {
                            try {
                                Double value = cellule.getNumericCellValue();
                                stocker.setQuantite(value);
                                destock.setQuantite(value);
                                recq.setQuantite(value);
                            } catch (Exception e) {
                                MainUI.notify(null, "Error", "Erreur num-lot " + e.getMessage(), 5, "error");
                            }
                        }
                    }
                    break;
                case 9:
                    if (!isCellEmpty(cellule)) {
                        if (cellule.getCellType().equals(CellType.NUMERIC)) {
                            Double value = cellule.getNumericCellValue();
                            stocker.setStockAlerte(value);
                            recq.setStockAlert(value);
                        }
                    } else {
                        MainUI.notify(null, "Erreur",
                                "Le stock d'alerte est obligatoire, pour la ligne " + (row.getRowNum() + 1), 7,
                                "error");
                        return null;
                    }
                    break;
                case 10:
                    if (!isCellEmpty(cellule)) {
                        if (cellule.getCellType().equals(CellType.NUMERIC)) {
                            Double value = cellule.getNumericCellValue();
                            stocker.setCoutAchat(value);
                            recq.setCoutAchat(value);
                            destock.setCoutAchat(value);
                        }
                    }
                    break;
                case 11:
                    if (!isCellEmpty(cellule)) {
                        String value1 = cellule.getStringCellValue();
                        stocker.setLocalisation(value1);
                    }
                    break;
                case 12:
                    if (!isCellEmpty(cellule)) {
                        if (cellule.getCellType().equals(CellType.NUMERIC)) {
                            if (DateUtil.isCellDateFormatted(cellule)) {
                                Date exp = cellule.getDateCellValue();
                                stocker.setDateExpir(Constants.Datetime.toLocalDate(exp));
                                recq.setDateExpiry(Constants.Datetime.toLocalDate(exp));
                            }
                        }
                    }
                    break;
                case 13:
                    if (!isCellEmpty(cellule)) {
                        String priz = cellule.getStringCellValue();
                        if (!priz.startsWith("[")) {
                            MainUI.notify(null, "Erreur",
                                    "La notation du prix incorrecte symbole [ manquant au debut, pour la ligne "
                                            + (row.getRowNum() + 1),
                                    7, "error");
                            return null;
                        }
                        if (!priz.endsWith("]")) {
                            MainUI.notify(null, "Erreur",
                                    "La notation du prix incorrecte symbole ] manquant a la fin, pour la ligne "
                                            + (row.getRowNum() + 1),
                                    7, "error");
                            return null;
                        }

                        priz = priz.replace("[", "").replace("]", "");
                        List<PrixDeVente> prices = new ArrayList<>();
                        if (priz.contains(",")) {
                            String prixs[] = priz.split(",");
                            for (int i = 0; i < prixs.length; i++) {
                                String prix = prixs[i];
                                if (prix != null) {
                                    if (!prix.contains(":")) {
                                        MainUI.notify(null, "Erreur", "A l'indice [" + i
                                                + "] la notation du prix incorrecte symbole : manquant entre le prix unitaire et quantités, pour la ligne "
                                                + (row.getRowNum() + 1), 7, "error");
                                        return null;
                                    }
                                    String prixz[] = prix.split(":");
                                    String quants = prixz[0];
                                    if (!quants.contains("-")) {
                                        MainUI.notify(null, "Erreur", "A l'indice [" + i
                                                + "] la notation du prix incorrecte symbole - manquant entre Qmin et Qmax, pour la ligne "
                                                + (row.getRowNum() + 1), 7, "error");
                                        return null;
                                    }
                                    String qmin = quants.split("-")[0];
                                    String qmax = quants.split("-")[1];
                                    String pvs = prixz[1];
                                    PrixDeVente pv = new PrixDeVente(DataId.generate());
                                    pv.setDevise("USD");
                                    pv.setMesureId(mesure);
                                    double max = Double.parseDouble(qmax);
                                    double min = Double.parseDouble(qmin);
                                    pv.setPrixUnitaire(Double.parseDouble(pvs));
                                    if (min == max) {
                                        MainUI.notify(null, "Erreur",
                                                "Le prix de vente incorrecte Quantite minimale doit etre inferieure a quantite maximale ["
                                                        + min + " = " + max + "] a la ligne " + (row.getRowNum() + 1),
                                                7, "error");
                                        return null;
                                    }
                                    pv.setQmin(min);
                                    pv.setQmax(max);
                                    pv.setRecquisitionId(recq);
                                    prices.add(pv);
                                }
                            }
                        } else {
                            String prix = priz;

                            if (!prix.contains(":")) {
                                MainUI.notify(null, "Erreur",
                                        "La notation du prix incorrecte symbole : manquant entre le prix unitaire et quantités, pour la ligne "
                                                + (row.getRowNum() + 1),
                                        7, "error");
                                return null;
                            }
                            String prixz[] = prix.split(":");
                            String quants = prixz[0];
                            if (!quants.contains("-")) {
                                MainUI.notify(null, "Erreur",
                                        "La notation du prix incorrecte symbole - manquant entre Qmin et Qmax, pour la ligne "
                                                + (row.getRowNum() + 1),
                                        7, "error");
                                return null;
                            }
                            String qmin = quants.split("-")[0];
                            String qmax = quants.split("-")[1];
                            String pvs = prixz[1];
                            PrixDeVente pv = new PrixDeVente(DataId.generate());
                            pv.setDevise("USD");
                            pv.setMesureId(mesure);
                            double max = Double.parseDouble(qmax);
                            double min = Double.parseDouble(qmin);
                            pv.setPrixUnitaire(Double.parseDouble(pvs));
                            if (min == max) {
                                MainUI.notify(null, "Erreur",
                                        "Le prix de vente incorrecte Quantite minimale doit etre inferieure a quantite maximale ["
                                                + min + " = " + max + "] a la ligne " + (row.getRowNum() + 1),
                                        7, "error");
                                return null;
                            }
                            pv.setQmin(min);
                            pv.setQmax(max);
                            pv.setRecquisitionId(recq);
                            prices.add(pv);

                        }

                        importer.setSalesPrices(prices);
                    }
                    break;
                default:
                    break;

            }
        }
        destock.setDateDestockage(LocalDateTime.now());
        stocker.setDateStocker(LocalDateTime.now());
        recq.setDate(LocalDateTime.now());
        destock.setDestination(region);
        stocker.setRegion(region);
        recq.setRegion(region);
        destock.setRegion(region);
        destock.setReference(ref);
        recq.setReference(ref);
        stocker.setReduction(0);
        stocker.setLibelle("importation");
        destock.setProductId(produit);
        destock.setLibelle("importation");
        destock.setObservation("-");
        stocker.setObservation("-");
        recq.setObservation("-");
        stocker.setProductId(produit);
        recq.setProductId(produit);
        stocker.setPrixAchatTotal(stocker.getCoutAchat() * stocker.getQuantite());
        importer.setMesure(mesure);
        importer.setDestocker(destock);
        importer.setProduit(produit);
        importer.setRecquisition(recq);
        importer.setStocker(stocker);
        return importer;
    }

    private static LigneImport findLigne(List<LigneImport> llis, String codebar) {
        for (LigneImport lli : llis) {
            if (lli == null) {
                return null;
            }
            Produit x = lli.getProduit();
            if (x.getCodebar().equals(codebar)) {
                return lli;
            }
        }
        return null;
    }

    private static List<LigneImport> help(File f, List<Produit> lprods, String region, boolean conv, String ref,
            String meth) {
        List<LigneImport> llis = new ArrayList<>();
        try {
            FileInputStream fis = new FileInputStream(f);
            HSSFWorkbook workbook = new HSSFWorkbook(fis);

            HSSFSheet sheet = workbook.getSheetAt(0);
            if (!sheet.getSheetName().equals("Grossiste")) {
                MainUI.notify(null, "Erreur", "La premiere feuile du fichier doit porter ne nom [Grossiste]", 3,
                        "error");
                return null;
            }
            System.out.println("in the function ");
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row r1 = rowIterator.next();
                if (isRowEmpty(r1)) {
                    if (r1.getRowNum() == 1) {
                        MainUI.notify(null, "Erreur", "La premiere ligne de la feuille ne doit pas etre vide", 7,
                                "error");
                    }
                    break;
                }
                if (r1.getRowNum() > 0) {
                    LigneImport imp = null;
                    Cell cell0 = r1.getCell(0);
                    if (!isCellEmpty(cell0)) {
                        if (cell0.getCellType().equals(CellType.STRING)) {
                            String cb = cell0.getStringCellValue();
                            imp = findLigne(llis, cb);
                        }
                    }
                    System.out.println("findline " + imp);
                    if (imp == null) {
                        Produit produit = new Produit(DataId.generate());
                        LigneImport impo = proceedImport(r1, produit, lprods, conv, region, ref, meth);
                        if (impo == null) {
                            MainUI.notify(null, "Attention !", "Certains elements n'ont pas pû être importés", 5,
                                    "warn");
                            break;
                        }
                        llis.add(impo);
                    } else {
                        Produit produit = imp.getProduit();
                        LigneImport li = proceedImport(r1, produit, lprods, conv, region, ref, meth);
                        if (li == null) {
                            MainUI.notify(null, "Attention !", "Certains elements n'ont pas pû être importés", 5,
                                    "warn");
                            break;
                        }
                        llis.add(li);
                    }
                    System.out.println("Size +++" + llis.size());
                }
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        return llis;
    }

    public static List<LigneImport> importGrosFromExcel(File f, List<Produit> lprods, String region, boolean conv,
            String ref, String meth) throws IllegalStateException {
        return help(f, lprods, region, conv, ref, meth);
    }

    private static boolean isCellEmpty(Cell cell) {
        boolean empty = true;
        DataFormatter dataFormatter = new DataFormatter();
        if (cell != null) {
            if (dataFormatter.formatCellValue(cell).trim().length() > 0) {
                empty = false;
            }
        }
        return empty;
    }

    private static boolean isRowEmpty(Row rows) {
        boolean isEmpty = true;
        DataFormatter dataFormatter = new DataFormatter();
        if (rows != null) {
            for (Cell cell : rows) {
                if (!dataFormatter.formatCellValue(cell).trim().isEmpty()) {
                    return false;
                }
            }
        }
        return isEmpty;
    }

    public static List<Produit> readProductFromExcel(File f) throws IllegalStateException {
        List<Produit> result = new ArrayList<>();
        try {
            FileInputStream fis = new FileInputStream(f);
            HSSFWorkbook workbook = new HSSFWorkbook(fis);
            HSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row r1 = rowIterator.next();
                if (r1.getRowNum() > 0) {
                    Iterator<Cell> cellIterator = r1.cellIterator();
                    Produit produit = new Produit(DataId.generate());
                    InputStream is = MainuiController.class.getResourceAsStream("/icons/gallery.png");
                    byte[] img = FileUtils.readAllBytes(is);
                    produit.setImage(img);
                    produit.setDateCreation(LocalDateTime.now());
                    while (cellIterator.hasNext()) {
                        Cell m = cellIterator.next();
                        switch (m.getColumnIndex()) {
                            case 0:
                                produit.setCodebar(m.getStringCellValue());
                                break;
                            case 1:
                                produit.setNomProduit(m.getStringCellValue());
                                break;
                            case 2:
                                produit.setMarque(m.getStringCellValue());
                                break;
                            case 3:
                                produit.setModele(m.getStringCellValue());
                                break;
                            case 4:
                                produit.setTaille(m.getStringCellValue());
                                break;
                            case 5:
                                produit.setCouleur(m.getStringCellValue());
                                break;

                        }
                    }

                    result.add(produit);
                }
            }
            return result;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static void setResourceImage(ImageView img, String filename) {

        InputStream is = MainuiController.class.getResourceAsStream("/icons/" + filename);
        File f = FileUtils.streamTofile(is);

        Image image = null;
        try {
            image = new Image(new FileInputStream(f));
            img.setImage(image);
            centerImage(img);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void installPicture(ImageView imgvu, String filename) {
        File f = FileUtils.pointFile(filename);
        InputStream is;
        if (!f.exists()) {
            is = MainuiController.class.getResourceAsStream("/icons/gallery.png");
            f = FileUtils.streamTofile(is);
        }
        Image image = null;
        try {
            image = new Image(new FileInputStream(f));
            imgvu.setImage(image);
            centerImage(imgvu);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ProduitsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static List<RequestHelper> filterRecquest(List<Mesure> lmz, List<PrixDeVente> pvs, List<Recquisition> lreq) {
        List<RequestHelper> result = new ArrayList<>();
        List<RequestHelper> arange = new ArrayList<>();
        System.err.println("Recq size " + lreq.size());
        for (Recquisition r : lreq) {
            RequestHelper rh = new RequestHelper();
            rh.setCoutAchat(r.getCoutAchat());
            rh.setDateReq(Constants.Datetime.toUtilDate(r.getDate().toLocalDate()));
            rh.setMesureId(r.getMesureId());
            rh.setStockAlerte(r.getStockAlert());
            rh.setDateExpiry(Constants.Datetime.toUtilDate(r.getDateExpiry()));
            rh.setProductId(r.getProductId());
            rh.setQuantite(r.getQuantite());
            rh.setRequid(r.getUid());
            List<PrixDeVente> prices = checkPrices(pvs, r.getUid());
            rh.setPrices(prices);
            if (!arange.contains(rh)) {
                arange.add(rh);
            }
        }
        Collections.sort(arange, new RequestHelper());
        for (RequestHelper rh : arange) {

            RequestHelper oneRh = unify(lmz, findForProduct(arange, rh.getProductId().getUid()),
                    rh.getProductId().getUid());
            if (!result.contains(oneRh)) {
                // double qpc = sumQForProduct(arange, lmz, rh.getProductId().getUid());
                // rh.setQuantiteVendu(qpc);
                result.add(oneRh);
            }
        }
        return result;
    }

    public static RequestHelper findRecent(List<Recquisition> lreq) {
        List<RequestHelper> arange = new ArrayList<>();
        for (Recquisition r : lreq) {
            RequestHelper rh = new RequestHelper();
            rh.setCoutAchat(r.getCoutAchat());
            rh.setDateReq(Constants.Datetime.toUtilDate(r.getDate().toLocalDate()));
            rh.setMesureId(r.getMesureId());
            rh.setStockAlerte(r.getStockAlert() == null ? 0d : r.getStockAlert());
            rh.setDateExpiry(Constants.Datetime.toUtilDate(r.getDateExpiry()));
            rh.setProductId(r.getProductId());
            rh.setQuantite(r.getQuantite());
            rh.setRequid(r.getUid());
            if (!arange.contains(rh)) {
                arange.add(rh);
            }
        }
        Collections.sort(arange, new RequestHelper());
        return arange.isEmpty() ? null : arange.get(arange.size() - 1);
    }

    public static Recquisition findRecentRecq(List<Recquisition> lreq) {
        List<RequestHelper> arange = new ArrayList<>();
        for (Recquisition r : lreq) {
            RequestHelper rh = new RequestHelper();
            rh.setCoutAchat(r.getCoutAchat());
            rh.setDateReq(Constants.Datetime.toUtilDate(r.getDate().toLocalDate()));
            rh.setMesureId(r.getMesureId());
            rh.setStockAlerte(r.getStockAlert());
            rh.setDateExpiry(Constants.Datetime.toUtilDate(r.getDateExpiry()));
            rh.setProductId(r.getProductId());
            rh.setQuantite(r.getQuantite());
            rh.setRequid(r.getUid());
            if (!arange.contains(rh)) {
                arange.add(rh);
            }
        }
        Collections.sort(arange, new RequestHelper());
        if (arange.isEmpty()) {
            return null;
        }
        RequestHelper reqh = arange.get(arange.size() - 1);
        Recquisition req = findRecqById(lreq, reqh.getRequid());
        return req;
    }

    private static List<Recquisition> findRecqForProd(List<Recquisition> lreq, String prodId) {
        List<Recquisition> arange = new ArrayList<>();
        for (Recquisition r : lreq) {
            if (r.getProductId().getUid().equals(prodId)) {
                arange.add(r);
            }
        }
        return arange;
    }

    private static Recquisition findRecqById(List<Recquisition> lreq, String reqId) {
        for (Recquisition r : lreq) {
            if (r.getUid().equals(reqId)) {
                return r;
            }
        }
        return null;
    }

    public static List<PrixDeVente> checkPrices(List<PrixDeVente> ps, String reqUid) {
        List<PrixDeVente> result = new ArrayList<>();
        for (PrixDeVente p : ps) {
            if (p.getRecquisitionId().getUid().equals(reqUid)) {
                result.add(p);
            }
        }
        return result;
    }

    public static PrixDeVente findAvailablePrice(List<PrixDeVente> ps) {
        for (PrixDeVente p : ps) {
            if (p != null) {
                double pr = p.getPrixUnitaire();
                double qmin = p.getQmin();
                if (pr > 0 || qmin > 0) {
                    return p;
                }
            }
        }
        return null;
    }

    public static double sumCart(List<LigneVente> lvs, String dev) {
        if (lvs == null) {
            return 0;
        }
        double rst = 0;
        for (LigneVente lv : lvs) {
            if (dev.equals("USD")) {
                rst += lv.getMontantUsd();
            } else {
                rst += lv.getMontantCdf();
            }
        }
        return rst;
    }

    public static PrixDeVente findPrice(List<PrixDeVente> ps, String uid) {
        for (PrixDeVente p : ps) {
            if (p.getUid().equals(uid)) {
                return p;
            }
        }
        return null;
    }

    public static int getIndex(List<PrixDeVente> ps, String uid) {
        for (PrixDeVente p : ps) {
            if (p.getUid().equals(uid)) {
                return ps.indexOf(p);
            }
        }
        return -1;
    }

    public static PrixDeVente findPrice(List<PrixDeVente> ps, Mesure m, double qu) {
        try {
            if (ps != null) {
                for (PrixDeVente p : ps) {
                    if (p.getMesureId().getDescription().equalsIgnoreCase(m.getDescription())) {
                        double min = p.getQmin(), max = p.getQmax();
                        if (min <= qu && max >= qu) {
                            return p;
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static RequestHelper unify(List<Mesure> lmz, List<RequestHelper> lreqh, String idPro) {
        double quant = 0;

        Collections.sort(lreqh, new RequestHelper());
        RequestHelper last = lreqh.get(lreqh.size() - 1);
        Mesure recent = findMesure(lmz, last.getMesureId());
        for (RequestHelper rh : lreqh) {
            if (rh.getProductId().getUid().equals(idPro)) {
                quant = sumQForProduct(lreqh, lmz, rh.getProductId().getUid());
            }
        }
        System.err.println("QPC " + quant);
        last.setQuantite(quant);
        last.setMesureId(recent);
        return last;
    }

    public static double sumQForProduct(List<RequestHelper> lrh, List<Mesure> lmz, String prod) {
        double q = 0;
        for (RequestHelper rh : lrh) {
            if (rh.getProductId().getUid().equals(prod)) {
                Mesure m = findMesure(lmz, rh.getMesureId());
                q += (rh.getQuantite() * m.getQuantContenu());
            }
        }
        return q;
    }

    public static List<RequestHelper> findForProduct(List<RequestHelper> lrh, String prod) {
        List<RequestHelper> result = new ArrayList<>();
        for (RequestHelper rh : lrh) {
            if (rh.getProductId().getUid().equals(prod)) {
                if (!result.contains(rh)) {
                    result.add(rh);
                }
            }
        }
        return result;
    }

    public static double getRecqSumForProd(List<Mesure> lmz, List<Recquisition> lr, String idPro) {
        double rst = 0;
        for (Recquisition r : lr) {
            if (r.getProductId().getUid().equals(idPro)) {
                Mesure m = r.getMesureId();
                rst += (m.getQuantContenu() * r.getQuantite());
            }
        }
        return rst;
    }

    public static double getLigneVenteSumForProd(List<Mesure> lmz, List<LigneVente> llv, String idPro) {
        double rst = 0;
        for (LigneVente lv : llv) {
            if (lv.getProductId().getUid().equals(idPro)) {
                Mesure m = findMesure(lmz, lv.getMesureId());
                rst += (m.getQuantContenu() * lv.getQuantite());
            }
        }
        return rst;
    }

    public static void centerImage(ImageView imageView) {
        Image img = imageView.getImage();
        if (img != null) {
            double w = 0;
            double h = 0;

            double ratioX = imageView.getFitWidth() / img.getWidth();
            double ratioY = imageView.getFitHeight() / img.getHeight();

            double reducCoeff = 0;
            if (ratioX >= ratioY) {
                reducCoeff = ratioY;
            } else {
                reducCoeff = ratioX;
            }

            w = img.getWidth() * reducCoeff;
            h = img.getHeight() * reducCoeff;

            imageView.setX((imageView.getFitWidth() - w) / 2);
            imageView.setY((imageView.getFitHeight() - h) / 2);

        }
    }

    public static Stocker findLastStocker(List<Stocker> lstk) {
        List<StockerComparator> stpool = new ArrayList<>();
        for (Stocker stk : lstk) {
            StockerComparator stc = new StockerComparator(stk.getDateStocker(),
                    stk.getUid(), stk.getMesureId().getUid(), stk.getProductId().getUid());
            stpool.add(stc);
        }
        Collections.sort(stpool, new StockerComparator());
        Collections.reverse(stpool);
        StockerComparator stkc = stpool.isEmpty() ? null : stpool.get(stpool.size() - 1);
        return stkc == null ? null : findStocker(lstk, stkc.getUid());
    }

    public static Stocker findStocker(List<Stocker> stks, String uid) {
        for (Stocker stk : stks) {
            if (stk.getUid().equalsIgnoreCase(uid)) {
                return stk;
            }
        }
        return null;
    }

    public static Mesure findMesure(List<Mesure> lismez, Mesure mzr) {
        String uid = mzr.getUid();
        for (Mesure m : lismez) {
            if (m.getUid().equalsIgnoreCase(uid)) {
                return m;
            }
        }
        return mzr;
    }

    public static List<Mesure> findMesureForProduitWithId(List<Mesure> allMez, String produitId) {
        List<Mesure> lmz = new ArrayList<>();
        for (Mesure m : allMez) {
            if (m.getProduitId().getUid().equals(produitId)) {
                lmz.add(m);
            }
        }
        return lmz;
    }

    public static Collection<Produit> findProduitInLivraison(List<Stocker> allMez) {
        Set<Produit> lmz = new HashSet<>();
        for (Stocker m : allMez) {
            lmz.add(m.getProductId());
        }
        return lmz;
    }

    public static String numfyFrenchChars(String frenchChar) {
        StringBuilder sb = new StringBuilder();
        String letters = "&é\"'(§è!çà-_";
        String numbers = "123456789068";
        for (char c : frenchChar.toCharArray()) {
            boolean notfound = true;
            for (char d : letters.toCharArray()) {
                if (c == d) {
                    notfound = false;
                    sb.append(numbers.charAt(letters.indexOf(d)));
                }
            }
            if (notfound) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static double sumDestockerQuantInPc(List<Destocker> lsd) {
        double sum = 0;
        for (Destocker destocker : lsd) {
            Mesure ms = destocker.getMesureId();
            double qin = ms.getQuantContenu();
            sum += destocker.getQuantite() * qin;
        }
        return sum;
    }

    public static double sumLigneVenteQuantInPc(List<LigneVente> lsd) {
        double sum = 0;
        for (LigneVente destocker : lsd) {
            Mesure ms = destocker.getMesureId();
            double qin = ms.getQuantContenu();
            sum += destocker.getQuantite() * qin;
        }
        return sum;
    }

    public static double getCump(List<Mesure> lmz, Mesure mz, List<Stocker> lst, Produit p) {
        List<Stocker> listsk = findStockersForProduit(lst, p.getUid());
        double quants = 0;
        double values = 0;
        for (Stocker s : listsk) {
            // String uid = .getUid();
            Mesure mzr = findMesure(lmz, s.getMesureId());
            double qpc = s.getQuantite() * mzr.getQuantContenu();
            quants += qpc;
            values += (s.getCoutAchat() / mzr.getQuantContenu()) * qpc;
        }
        double c = quants == 0 ? 0 : (values / quants);
        System.err.println("valeur " + c);
        double rst = BigDecimal.valueOf(c).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        return values == 0 ? 0 : rst * (mz == null ? 1 : mz.getQuantContenu());
    }

    public static Livraison findLivraisonByPiece(List<Livraison> lvs, String npc) {
        for (Livraison l : lvs) {
            if (l.getNumPiece().equals(npc)) {
                return l;
            }
        }
        return null;
    }

    public static long countProduitInStock(List<Stocker> stocks) {
        long i = 0;
        Set<Produit> pass = new HashSet<>();
        for (Stocker stock : stocks) {
            Produit p = stock.getProductId();
            List<Stocker> spro = findStockersForProduit(stocks, p.getUid());
            if (!spro.isEmpty()) {
                if (pass.add(p)) {
                    i++;
                }
            }
        }
        return i;
    }

    public static List<Stocker> findStockerForLivraison(List<Stocker> allStk, String livUid) {
        List<Stocker> result = new ArrayList<>();
        for (Stocker s : allStk) {
            if (s.getLivraisId().getUid().equals(livUid)) {
                result.add(s);
            }
        }
        return result;
    }

    public static Livraison findLivraison(List<Livraison> lv, String livId) {
        for (Livraison l : lv) {
            if (l.getUid().equals(livId)) {
                return l;
            }
        }
        return null;
    }

    public static List<Fournisseur> findFournisseur(List<Fournisseur> fourniseur, String query) {
        List<Fournisseur> result = new ArrayList<>();
        for (Fournisseur four : fourniseur) {
            String v = four.getAdresse() + " " + four.getIdentification() + " " + four.getNomFourn() + " "
                    + four.getPhone();
            if (v.toUpperCase().contains(query.toUpperCase())) {
                result.add(four);
            }
        }
        return result;
    }

    public static Mesure findMaxMesureForProduit(List<Mesure> lmz, String pro) {

        System.err.println("Mesure pour prod " + pro + " " + lmz.size());
        double max = 1;
        for (Mesure m : lmz) {
            double qc = m.getQuantContenu();
            for (Mesure x : lmz) {
                double qcc = x.getQuantContenu();
                if (qc < qcc) {
                    max = qcc;
                }
            }
        }
        System.err.println("maxz = " + max);
        return findMesureByQuant(lmz, max);
    }

    public static <T> double sumQuantInPc(List<T> objs) {
        double sum = 0;
        for (Object obj : objs) {
            if (obj instanceof Recquisition) {

                Recquisition recquisition = (Recquisition) obj;
                if (recquisition.getReference().startsWith("RTR")) {
                    continue;
                }
                Mesure mez = recquisition.getMesureId();
                Double qin = mez.getQuantContenu();
                sum += (recquisition == null ? 0 : recquisition.getQuantite()) * (qin == null ? 0 : qin);
                System.out.println("SUm req++  " + sum + " " + recquisition);
            } else if (obj instanceof LigneVente) {
                LigneVente ligneVente = (LigneVente) obj;
                Mesure m = ligneVente.getMesureId();
                Double qin = (m == null) ? 1 : m.getQuantContenu();
                sum += ligneVente.getQuantite() * (qin == null ? 0 : qin);
            } else if (obj instanceof Stocker) {
                Stocker stocker = (Stocker) obj;
                Mesure m = stocker.getMesureId();
                sum += stocker.getQuantite() * m.getQuantContenu();
            } else if (obj instanceof Destocker) {
                Destocker dest = (Destocker) obj;
                Mesure m = dest.getMesureId();
                sum += dest.getQuantite() * m.getQuantContenu();
            }
        }
        return sum;
    }

    public static Mesure getMinMesure(List<Mesure> lm) {
        double n = 0;
        Mesure rst = null;
        if (lm.size() <= 1 && lm.size() != 0) {
            return lm.get(0);
        }
        for (Mesure m : lm) {
            for (Mesure m1 : lm) {
                if (m1.getQuantContenu() < m.getQuantContenu()) {
                    n = m1.getQuantContenu();
                    rst = m1;
                }
            }
        }
        return rst;
    }

    public static PrixDeVente findPriceForMesure(List<PrixDeVente> prices, Mesure mesure) {
        for (PrixDeVente price : prices) {
            System.out.println("Price loop " + price.getMesureId() + " arg " + mesure);
            if (price.getMesureId().getDescription().equalsIgnoreCase(mesure.getDescription())) {
                return price;
            }
        }
        return null;
    }

    public static PrixDeVente findValuedMeasure(List<Mesure> mzprod, List<PrixDeVente> lastReqPvs) {
        for (Mesure m : mzprod) {
            for (PrixDeVente pv : lastReqPvs) {
                if (m.getUid().equals(pv.getMesureId().getUid())) {
                    return pv;
                }
            }
        }
        return null;
    }

    public static Mesure findMesureByQuant(List<Mesure> lismez, double q) {
        for (Mesure m : lismez) {
            if (m.getQuantContenu() == q) {
                return m;
            }
        }
        return null;
    }

    public static Mesure findMesureByDesc(List<Mesure> lismez, String desc) {
        for (Mesure m : lismez) {
            if (m.getDescription().equalsIgnoreCase(desc)) {
                return m;
            }
        }
        return null;
    }

    public static List<Traisorerie> findTresoreriesFor(List<Traisorerie> ltz, String compte) {
        List<Traisorerie> result = new ArrayList<>();
        for (Traisorerie t : ltz) {
            if (t.getTypeTresorerie().equals(compte)) {
                result.add(t);
            }
        }
        return result;
    }

    public static List<Traisorerie> findTresoreries(List<Traisorerie> ltz, String movment, String ref) {
        List<Traisorerie> result = new ArrayList<>();
        for (Traisorerie t : ltz) {
            if (t.getMouvement().equals(movment)
                    && t.getReference().toUpperCase().contains(ref.toUpperCase())) {
                result.add(t);
            }
        }
        return result;
    }

    public static List<Stocker> findStockersForProduit(List<Stocker> allStk, String prod) {
        List<Stocker> result = new ArrayList<>();
        for (Stocker s : allStk) {
            Produit p = s.getProductId();
            if (p == null) {
                continue;
            }
            if (p.getUid().equals(prod)) {
                result.add(s);
            }
        }
        return result;
    }

    public static List<Stocker> findStockersForProduit(List<Stocker> allStk, String prod, String region) {
        List<Stocker> result = new ArrayList<>();
        for (Stocker s : allStk) {
            if (s.getRegion() == null) {
                continue;
            }
            Produit p = s.getProductId();
            if (p == null) {
                continue;
            }
            if (p.getUid().equals(prod) && s.getRegion().equals(region)) {
                result.add(s);
            }
        }
        return result;
    }

    public static List<Stocker> findStockersForProduit(List<Stocker> allStk, String prod, long d1, long d2) {
        List<Stocker> result = new ArrayList<>();
        for (Stocker s : allStk) {
            long d = s.getDateStocker().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            Produit p = s.getProductId();
            if (p == null) {
                continue;
            }
            if (p.getUid().equals(prod) && (d >= d1 && d <= d2)) {
                result.add(s);
            }
        }
        return result;
    }

    public static List<Stocker> findStockersInterval(List<Stocker> allStk, long d1, long d2) {
        List<Stocker> result = new ArrayList<>();
        for (Stocker s : allStk) {
            long d = s.getDateStocker().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            if ((d >= d1 && d <= d2)) {
                result.add(s);
            }
        }
        return result;
    }

    public static List<Livraison> findLivraisonsInterval(List<Livraison> alliv, long d1, long d2) {
        List<Livraison> result = new ArrayList<>();
        for (Livraison s : alliv) {
            long d = s.getDateLivr().toEpochSecond(LocalTime.now(), ZoneOffset.ofHours(2));
            if ((d >= d1 && d <= d2)) {
                result.add(s);
            }
        }
        return result;
    }

    public static List<Destocker> findDestockersForProduit(List<Destocker> allStk, String prod) {
        List<Destocker> result = new ArrayList<>();
        for (Destocker s : allStk) {
            Produit p = s.getProductId();
            if (p == null) {
                continue;
            }
            if (p.getUid().equals(prod)) {
                result.add(s);
            }
        }
        return result;
    }

    public static List<Destocker> findDestockersForProduit(List<Destocker> allStk, String prod, String region) {
        List<Destocker> result = new ArrayList<>();
        for (Destocker s : allStk) {
            if (s.getRegion() == null) {
                continue;
            }
            if (s.getProductId().getUid().equals(prod) && s.getRegion().equals(region)) {
                result.add(s);
            }
        }
        return result;
    }

    public static Destocker findDestockerByRef(List<Destocker> allStk, String ref) {
        for (Destocker s : allStk) {
            if (s.getReference().equals(ref)) {
                return s;
            }
        }
        return null;
    }

    public static Destocker findDestockerByProd(List<Destocker> allStk, String prod) {
        for (Destocker s : allStk) {
            Produit p = s.getProductId();
            if (p == null) {
                continue;
            }
            if (p.getUid().equals(prod)) {
                return s;
            }
        }
        return null;
    }

    public static Recquisition findRecqByProd(List<Recquisition> allStk, String prod) {
        for (Recquisition s : allStk) {
            Produit p = s.getProductId();
            if (p == null) {
                continue;
            }
            if (p.getUid().equals(prod)) {
                return s;
            }
        }
        return null;
    }

    public static Recquisition findRecqByRef(List<Recquisition> allStk, String ref) {
        for (Recquisition s : allStk) {
            if (s.getReference().equals(ref)) {
                return s;
            }
        }
        return null;
    }

    public static Recquisition findRecquisition(List<Recquisition> allStk, String uid) {
        for (Recquisition s : allStk) {
            if (s.getUid().equals(uid)) {
                return s;
            }
        }
        return null;
    }

    public static List<String> extractRecIds(List<Recquisition> lrs) {
        List<String> result = new ArrayList<>();
        for (Recquisition lr : lrs) {
            result.add(lr.getUid());
        }
        return result;
    }

    public static List<Destocker> findDestockersForProduit(List<Destocker> allStk, String prod, long d1, long d2) {
        List<Destocker> result = new ArrayList<>();
        for (Destocker s : allStk) {
            long d = s.getDateDestockage().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            Produit p = s.getProductId();
            if (p == null) {
                continue;
            }
            if (p.getUid().equals(prod) && (d >= d1 && d <= d2)) {
                result.add(s);
            }
        }
        return result;
    }

    /**
     *
     * @param allStk
     * @param prod
     * @return
     */
    public static List<Recquisition> findRequisitionForProduit(List<Recquisition> allStk, String prod) {
        List<Recquisition> result = new ArrayList<>();
        for (Recquisition s : allStk) {
            Produit p = s.getProductId();
            if (p == null) {
                continue;
            }
            if (p.getUid().equals(prod)) {
                result.add(s);
            }
        }
        return result;
    }

    public static List<Recquisition> findRequisitionForProduit(List<Recquisition> allStk, String prod, String region) {
        List<Recquisition> result = new ArrayList<>();
        for (Recquisition s : allStk) {
            String reg = s.getRegion();
            if (reg == null) {
                continue;
            }
            Produit p = s.getProductId();
            if (p == null) {
                continue;
            }
            if (region != null) {
                if (p.getUid().equals(prod) && reg.equals(region)) {
                    result.add(s);
                }
            } else {
                if (p.getUid().equals(prod)) {
                    result.add(s);
                }
            }

        }
        return result;
    }

    public static List<Recquisition> findRequisitionForProduit(List<Recquisition> allStk, String prod, long d1,
            long d2) {
        List<Recquisition> result = new ArrayList<>();
        for (Recquisition s : allStk) {
            long d = s.getDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            Produit p = s.getProductId();
            if (p == null) {
                continue;
            }
            if (p.getUid().equals(prod) && (d >= d1 && d <= d2)) {
                result.add(s);
            }
        }
        return result;
    }

    public static List<LigneVente> findLigneVenteForProduit(List<LigneVente> allStk, String prod) {
        List<LigneVente> result = new ArrayList<>();
        for (LigneVente s : allStk) {
            Produit p = s.getProductId();
            if (p == null) {
                continue;
            }
            if (p.getUid().equals(prod)) {
                result.add(s);
            }
        }
        return result;
    }

    public static Stocker findLastStock(List<Stocker> stocker) {
        List<StockerComparator> lsc = new ArrayList<>();
        for (Stocker s : stocker) {
            StockerComparator sc = new StockerComparator();
            sc.setMesureId(s.getMesureId().getUid());
            sc.setProductId(s.getProductId().getUid());
            sc.setStockDate(s.getDateStocker());
            sc.setUid(s.getUid());
            lsc.add(sc);
        }
        Collections.sort(lsc, new StockerComparator());
        Collections.reverse(lsc);
        return findStocker(stocker, lsc.get(0).getUid());
    }

    public static List<LigneVente> getLigneVenteForVente(List<LigneVente> allStk, int vid) {
        List<LigneVente> result = new ArrayList<>();
        for (LigneVente s : allStk) {
            if (s.getReference() == null) {
                continue;
            }
            if (s.getReference().getUid() == vid) {
                result.add(s);
            }
        }
        return result;
    }

    public static List<LigneVente> findLigneVenteForProduit(List<Vente> allStk, List<LigneVente> allv, String prod,
            String region) {
        List<LigneVente> result = new ArrayList<>();
        List<LigneVente> result0 = new ArrayList<>();
        List<Vente> lventes = getVenteForRegion(allStk, region);
        for (Vente vente : lventes) {
            List<LigneVente> lgv = vente.getLigneVenteList();
            if (lgv != null) {
                if (!result0.containsAll(lgv)) {
                    result0.addAll(lgv);
                }
            } else {
                List<LigneVente> lvs = getLigneVenteForVente(allv, vente.getUid());
                if (!result0.containsAll(lvs)) {
                    result0.addAll(lvs);
                }
            }
        }
        for (LigneVente s : result0) {
            Produit p = s.getProductId();
            if (p == null) {
                continue;
            }
            if (p.getUid().equals(prod)) {
                result.add(s);
            }
        }

        return result;
    }

    public static Vente findVenteByItem(List<Vente> lv, long uid) {
        for (Vente v : lv) {
            List<LigneVente> lig = v.getLigneVenteList();
            for (LigneVente ll : lig) {
                if (ll.getUid() == uid) {
                    return v;
                }
            }
        }
        return null;
    }

    public static double proportionalizeQuantity(double totalFacture, double totalCash, double quantActuel) {
        return (quantActuel * (totalCash / totalFacture));
    }

    public static List<Vente> getVentes(List<Vente> ventes, double taux) {
        List<Vente> result = new ArrayList<>();
        if (ventes != null) {
            for (Vente vente : ventes) {
                List<Vente> sameRef = getVentesWithRef(ventes, vente.getReference());
                if (sameRef.size() > 1) {
                    Vente compacted = compact(sameRef, taux);
                    result.add(compacted);
                    continue;
                }
                result.add(sameRef.get(0));
            }
        }
        return result;
    }

    public static Vente compact(List<Vente> ventes, double taux) {
        Vente vx = new Vente();
        double d = 0, f = 0, dt = 0;
        List<LigneVente> lgvs = new ArrayList<>();
        for (Vente vente : ventes) {
            vx.setClientId(vente.getClientId());
            vx.setDateVente(vente.getDateVente());
            if (vx.getEcheance() == null) {
                vx.setEcheance(vente.getEcheance());
            }
            vx.setDeviseDette(vente.getDeviseDette());
            vx.setLatitude(vente.getLatitude());
            vx.setLongitude(vente.getLongitude());
            vx.setLibelle(vente.getLibelle());
            vx.setReference(vente.getReference());
            vx.setRegion(vente.getRegion());
            vx.setObservation(vente.getObservation());
            d += vente.getMontantUsd();
            f += vente.getMontantCdf();
            dt += vente.getMontantDette() == null ? 0 : vente.getMontantDette();
            List<LigneVente> lignev = vente.getLigneVenteList();
            if (lignev == null) {
                continue;
            }
            lgvs.addAll(lignev);
        }
        vx.setPayment(Constants.PAYMENT_CREDIT_CASH);
        vx.setMontantCdf(f);
        vx.setMontantDette(dt);
        vx.setMontantUsd(d);
        vx.setUid(Integer.parseInt(vx.getReference()));
        List<LigneVente> lts = new ArrayList<>();
        for (LigneVente lgv : lgvs) {
            LigneVente lv = new LigneVente();
            Produit prod = lgv.getProductId();
            lv.setProductId(prod);
            List<LigneVente> prolist = findLigneVenteForProduit(lgvs, prod.getUid());
            double compactedQuant = sumQuantForCompact(prolist);
            double montant = compactedQuant * lgv.getPrixUnit();
            lv.setClientId(lgv.getClientId());
            lv.setMesureId(lgv.getMesureId());
            lv.setMontantCdf(montant * taux);
            lv.setMontantUsd(montant);
            lv.setPrixUnit(lgv.getPrixUnit());
            lv.setQuantite(compactedQuant);
            lv.setReference(lgv.getReference());
            lv.setUid(System.currentTimeMillis());
            List<LigneVente> lpro = findLigneVenteForProduit(lts, prod.getUid());
            if (lpro.isEmpty()) {
                lts.add(lv);
            }
        }
        vx.setLigneVenteList(lts);
        return vx;
    }

    public static double sumQuantForCompact(List<LigneVente> lvs) {
        double q = 0;
        for (LigneVente lv : lvs) {
            q += lv.getQuantite();
        }
        return q;
    }

    public static List<Vente> splitVente(Vente vente, double taux) {
        List<Vente> result = new ArrayList<>();
        double sommeCash = vente.getMontantUsd() + (vente.getMontantCdf() / taux);
        double sommeDt = vente.getMontantDette();
        double total = (sommeCash + sommeDt);
        System.out.println("cash = " + sommeCash + " dette = " + sommeDt);

        // cash
        Vente cash = new Vente();
        cash.setUid((int) (Math.random() * 10000000));
        cash.setClientId(vente.getClientId());
        cash.setDateVente(vente.getDateVente());
        cash.setDeviseDette(vente.getDeviseDette());
        cash.setLatitude(vente.getLatitude());
        cash.setLibelle(vente.getLibelle());
        cash.setLongitude(vente.getLongitude());
        cash.setObservation(vente.getObservation());
        cash.setReference(vente.getReference());
        cash.setRegion(vente.getRegion());
        cash.setPayment(Constants.PAYMENT_CASH);
        cash.setMontantCdf(vente.getMontantCdf());
        cash.setMontantUsd(vente.getMontantUsd());
        cash.setMontantDette(0d);
        cash.setEcheance(null);
        List<LigneVente> lvs = vente.getLigneVenteList();
        List<LigneVente> lvcash = new ArrayList<>();
        for (LigneVente lv : lvs) {
            LigneVente l = new LigneVente(lv.getUid());
            double newQuant = proportionalizeQuantity(total, sommeCash, lv.getQuantite());
            double usd = newQuant * lv.getPrixUnit();
            l.setQuantite(newQuant);
            l.setMontantUsd(usd);
            l.setMontantCdf(usd * taux);
            l.setClientId(lv.getClientId());
            l.setMesureId(lv.getMesureId());
            l.setPrixUnit(lv.getPrixUnit());
            l.setProductId(lv.getProductId());
            l.setReference(lv.getReference());
            lvcash.add(l);
        }
        cash.setLigneVenteList(lvcash);

        // dette
        Vente dette = new Vente();
        dette.setUid((int) (Math.random() * 10000001));
        dette.setClientId(vente.getClientId());
        dette.setDateVente(vente.getDateVente());
        dette.setDeviseDette(vente.getDeviseDette());
        dette.setLatitude(vente.getLatitude());
        dette.setLibelle(vente.getLibelle());
        dette.setLongitude(vente.getLongitude());
        dette.setObservation(vente.getObservation());
        dette.setReference(vente.getReference());
        dette.setRegion(vente.getRegion());
        dette.setPayment(Constants.PAYEMENT_CREDIT);
        dette.setMontantCdf(0d);
        dette.setMontantUsd(0d);
        dette.setEcheance(vente.getEcheance());
        dette.setMontantDette(sommeDt);
        List<LigneVente> lvdette = new ArrayList<>();
        for (LigneVente lv : lvs) {
            LigneVente l = new LigneVente(System.currentTimeMillis());
            double newQuant = proportionalizeQuantity(total, sommeDt, lv.getQuantite());
            double usd = newQuant * lv.getPrixUnit();
            l.setQuantite(newQuant);
            l.setMontantUsd(usd);
            l.setMontantCdf(usd * taux);
            l.setClientId(lv.getClientId());
            l.setMesureId(lv.getMesureId());
            l.setPrixUnit(lv.getPrixUnit());
            l.setProductId(lv.getProductId());
            l.setReference(lv.getReference());
            lvdette.add(l);
        }
        dette.setLigneVenteList(lvdette);
        result.add(cash);
        result.add(dette);
        return result;
    }

    public static List<Vente> getVentesWithRef(List<Vente> lv, String ref) {
        List<Vente> result = new ArrayList<>();
        for (Vente v : lv) {
            if (v.getReference().equals(ref)) {
                result.add(v);
            }
        }
        return result;
    }

    public static double sumSales(List<Vente> vs, double taux) {
        double cdf = 0, usd = 0, dt = 0;
        for (Vente v : vs) {
            cdf += v.getMontantCdf();
            usd += v.getMontantUsd();
            dt += v.getMontantDette() == null ? 0 : v.getMontantDette();
        }
        double rst = (usd + dt + (cdf / taux));

        return rst;
    }

    public static double sumCreditSales(List<Vente> vs, double taux) {
        double dt = 0;
        for (Vente v : vs) {
            dt += v.getMontantDette() == null ? 0 : v.getMontantDette();
        }
        double rst = dt;
        return rst;
    }

    public static double sumOps(List<Operation> ops, double taux) {
        double cdf = 0, usd = 0;
        for (Operation v : ops) {
            cdf += v.getMontantCdf();
            usd += v.getMontantUsd();
        }
        return (usd + (cdf / taux));
    }

    public static List<LigneVente> findLigneVenteForProduit(List<Vente> lvts, List<LigneVente> allStk, String prod,
            long d1, long d2) {
        List<LigneVente> result = new ArrayList<>();
        for (LigneVente s : allStk) {
            Vente v = getVente(lvts, s.getReference().getUid());
            long d = v.getDateVente().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            Produit p = s.getProductId();
            if (p == null) {
                continue;
            }
            if (p.getUid().equals(prod) && (d >= d1 && d <= d2)) {
                result.add(s);
            }
        }
        return result;
    }

    public static Vente getVente(List<Vente> lv, Integer ref) {
        for (Vente v : lv) {
            if (v.getUid() == ref) {
                return v;
            }
        }
        return null;
    }

    public static HashMap<String, Double> findLotQuant(List<Stocker> stoklist, List<Destocker> destok) {
        HashMap<String, Double> result = new HashMap<>();
        for (Stocker stocker : stoklist) {
            String numlot = stocker.getNumlot();
            Mesure ms = stocker.getMesureId();
            if (numlot != null) {
                double sumdstk = 0;
                for (Destocker destocker : destok) {
                    if (destocker.getNumlot().equals(numlot)) {
                        Mesure m = destocker.getMesureId();
                        sumdstk += destocker.getQuantite() * m.getQuantContenu();
                    }
                }
                double e = stocker.getQuantite() * ms.getQuantContenu();
                double rst = e - sumdstk;
                result.put(numlot, rst);
            }
        }

        return result;

    }

    public static double getQRest(List<Stocker> ls, List<Destocker> ld, Mesure mx, Produit p) {
        List<Stocker> lst = Util.findStockersForProduit(ls, p.getUid());
        List<Destocker> lsd = Util.findDestockersForProduit(ld, p.getUid());
        double in = 0, out = 0;
        for (Stocker s : lst) {
            in += (s.getQuantite() * mx.getQuantContenu());
        }
        for (Destocker d : lsd) {
            out += (d.getQuantite() * mx.getQuantContenu());
        }
        return ((in - out) / mx.getQuantContenu());
    }

    public static File exportXlsInventory(List<InventoryItem> lisinvent) {
        FileOutputStream fos;
        try {
            String path = MainUI.cPath("/Media/inventories");
            File file = new File(path + "/ksf-inv" + Constants.TIMESTAMPED_FORMAT.format(new Date()) + ".xls");
            fos = new FileOutputStream(file);
            HSSFWorkbook hsswb = new HSSFWorkbook();
            // creation de la feuil
            HSSFSheet feuil = hsswb.createSheet("Inventaire global");
            int rowid = 0;
            // les entetes des colones
            HSSFRow row0 = feuil.createRow(rowid);
            Cell codebar = row0.createCell(0);
            codebar.setCellValue("CODEBAR");
            Cell nom_produit = row0.createCell(1);
            nom_produit.setCellValue("PRODUIT");
            Cell quantite_entree = row0.createCell(2);
            quantite_entree.setCellValue("QTE ENTREES");
            Cell quantite_sortie = row0.createCell(3);
            quantite_sortie.setCellValue("QTE SORTIES");
            Cell quantite_restant = row0.createCell(4);
            quantite_restant.setCellValue("QTE RESTANTS");
            Cell stock_alerte = row0.createCell(5);
            stock_alerte.setCellValue("STOCK-ALERTE");
            Cell date_expir = row0.createCell(6);
            date_expir.setCellValue("EXPIRATION");
            Cell localistion = row0.createCell(7);
            localistion.setCellValue("LOCALISATION");
            for (InventoryItem ii : lisinvent) {
                row0 = feuil.createRow(++rowid);
                Cell codebar1 = row0.createCell(0);
                codebar1.setCellValue(ii.getProduit().getCodebar());
                Cell nom_prod = row0.createCell(1);
                nom_prod.setCellValue(ii.getProduit().getNomProduit() + ""
                        + " " + ii.getProduit().getMarque() + " "
                        + "" + ii.getProduit().getModele() + ""
                        + " " + (ii.getProduit().getTaille() == null ? "" : ii.getProduit().getTaille())
                        + " " + (ii.getProduit().getCouleur() == null ? "" : ii.getProduit().getCouleur()));
                Cell quant_in = row0.createCell(2);
                quant_in.setCellValue(ii.getQuantEntree());
                Cell quant_out = row0.createCell(3);
                quant_out.setCellValue(ii.getQuantSortie());
                Cell quant_remain = row0.createCell(4);
                quant_remain.setCellValue(ii.getQuantRest());
                Cell stock_alerte1 = row0.createCell(5);
                stock_alerte1.setCellValue(ii.getStockAlerte());
                Cell dexpir = row0.createCell(6);
                LocalDate dexp = ii.getLastStocker().getDateExpir();
                dexpir.setCellValue(dexp == null ? "Non perissable" : dexp.toString());
                Cell localisation = row0.createCell(7);
                localisation.setCellValue(ii.getLastStocker().getLocalisation());
            }
            hsswb.write(fos);
            fos.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File exportXlsRecquisition(List<Recquisition> lisinvent) {
        FileOutputStream fos;
        try {
            String path = MainUI.cPath("/Media/inventories");
            File file = new File(path + "/ksf-inv" + Constants.TIMESTAMPED_FORMAT.format(new Date()) + ".xls");
            fos = new FileOutputStream(file);
            HSSFWorkbook hsswb = new HSSFWorkbook();
            // creation de la feuil
            HSSFSheet feuil = hsswb.createSheet("Inventaire global");
            int rowid = 0;
            // les entetes des colones
            HSSFRow row0 = feuil.createRow(rowid);
            Cell codebar = row0.createCell(0);
            codebar.setCellValue("DATE");
            Cell nom_produit = row0.createCell(1);
            nom_produit.setCellValue("REFERENCE");
            Cell quantite_entree = row0.createCell(2);
            quantite_entree.setCellValue("PRODUITS");
            Cell quantite_sortie = row0.createCell(3);
            quantite_sortie.setCellValue("QUANTITE");
            Cell stock_alerte = row0.createCell(4);
            stock_alerte.setCellValue("STOCK-ALERTE");
            Cell date_expir = row0.createCell(5);
            date_expir.setCellValue("EXPIRATION");
            Cell localistion = row0.createCell(6);
            localistion.setCellValue("OBSERVATION");
            for (Recquisition ii : lisinvent) {
                row0 = feuil.createRow(++rowid);
                Cell codebar1 = row0.createCell(0);
                codebar1.setCellValue(ii.getDate());
                Cell ref = row0.createCell(1);
                ref.setCellValue(ii.getReference());
                Cell nom_prod = row0.createCell(2);
                nom_prod.setCellValue(ii.getProductId().getNomProduit() + ""
                        + " " + ii.getProductId().getMarque() + " "
                        + "" + ii.getProductId().getModele() + ""
                        + " " + (ii.getProductId().getTaille() == null ? "" : ii.getProductId().getTaille())
                        + " " + (ii.getProductId().getCouleur() == null ? "" : ii.getProductId().getCouleur()));
                Cell quant_in = row0.createCell(3);
                quant_in.setCellValue(ii.getQuantite() + " " + ii.getMesureId().getDescription());
                Cell quant_out = row0.createCell(4);
                quant_out.setCellValue(ii.getStockAlert() + " " + ii.getMesureId().getDescription());
                Cell dexpir = row0.createCell(5);
                LocalDate dexp = ii.getDateExpiry();
                dexpir.setCellValue(dexp == null ? "Non perissable" : dexp.toString());
                Cell localisation = row0.createCell(6);
                localisation.setCellValue(ii.getObservation());
            }
            hsswb.write(fos);
            fos.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File exportXlsTransactions(List<Transaction> transaction, CompteTresor account, double balanceUsd,
            double balanceCdf) {
        FileOutputStream fos;
        try {
            String path = MainUI.cPath("/Media/autres");
            File file = new File(path + "/ksf-Tresoreries" + Constants.TIMESTAMPED_FORMAT.format(new Date()) + ".xls");
            fos = new FileOutputStream(file);
            HSSFWorkbook hsswb = new HSSFWorkbook();
            HSSFSheet feuil = hsswb.createSheet("Tresorerie");
            int rowid = 0;

            // Account Info Header
            HSSFRow headerRow1 = feuil.createRow(rowid++);
            Cell accountLabel = headerRow1.createCell(0);
            accountLabel.setCellValue("COMPTE TRESOR:");
            Cell accountVal = headerRow1.createCell(1);
            accountVal.setCellValue(account != null ? (account.getIntitule() + " - " + account.getBankName()) : "-");

            HSSFRow headerRow2 = feuil.createRow(rowid++);
            Cell numLabel = headerRow2.createCell(0);
            numLabel.setCellValue("NUMERO:");
            Cell numVal = headerRow2.createCell(1);
            numVal.setCellValue(account != null ? account.getNumeroCompte() : "-");

            HSSFRow headerRow3 = feuil.createRow(rowid++);
            Cell balUsdLabel = headerRow3.createCell(0);
            balUsdLabel.setCellValue("SOLDE USD:");
            Cell balUsdVal = headerRow3.createCell(1);
            balUsdVal.setCellValue(balanceUsd);

            HSSFRow headerRow4 = feuil.createRow(rowid++);
            Cell balCdfLabel = headerRow4.createCell(0);
            balCdfLabel.setCellValue("SOLDE CDF:");
            Cell balCdfVal = headerRow4.createCell(1);
            balCdfVal.setCellValue(balanceCdf);

            rowid++; // Spacer

            // Column Headers
            HSSFRow rowHeaders = feuil.createRow(rowid++);
            rowHeaders.createCell(0).setCellValue("DATE");
            rowHeaders.createCell(1).setCellValue("REFERENCE");
            rowHeaders.createCell(2).setCellValue("REGION");
            rowHeaders.createCell(3).setCellValue("LIBELLE");
            rowHeaders.createCell(4).setCellValue("DEBIT USD");
            rowHeaders.createCell(5).setCellValue("DEBIT CDF");
            rowHeaders.createCell(6).setCellValue("CREDIT USD");
            rowHeaders.createCell(7).setCellValue("CREDIT CDF");
            rowHeaders.createCell(8).setCellValue("SOLDE USD");
            rowHeaders.createCell(9).setCellValue("SOLDE CDF");

            double totalDebitUsd = 0, totalDebitCdf = 0, totalCreditUsd = 0, totalCreditCdf = 0;

            for (Transaction ii : transaction) {
                HSSFRow row = feuil.createRow(rowid++);
                row.createCell(0).setCellValue(ii.getDate().toLocalDate().toString());
                row.createCell(1).setCellValue(ii.getReference());
                row.createCell(2).setCellValue(ii.getRegion());
                row.createCell(3).setCellValue(ii.getLibelle());
                row.createCell(4).setCellValue(ii.getDebit_usd());
                row.createCell(5).setCellValue(ii.getDebit_cdf());
                row.createCell(6).setCellValue(ii.getCredit_usd());
                row.createCell(7).setCellValue(ii.getCredit_cdf());
                row.createCell(8).setCellValue(ii.getSolde_usd());
                row.createCell(9).setCellValue(ii.getSolde_cdf());

                totalDebitUsd += ii.getDebit_usd();
                totalDebitCdf += ii.getDebit_cdf();
                totalCreditUsd += ii.getCredit_usd();
                totalCreditCdf += ii.getCredit_cdf();
            }

            // Totals Row
            HSSFRow totalsRow = feuil.createRow(rowid++);
            totalsRow.createCell(3).setCellValue("TOTAL:");
            totalsRow.createCell(4).setCellValue(totalDebitUsd);
            totalsRow.createCell(5).setCellValue(totalDebitCdf);
            totalsRow.createCell(6).setCellValue(totalCreditUsd);
            totalsRow.createCell(7).setCellValue(totalCreditCdf);

            for (int i = 0; i < 10; i++) {
                feuil.autoSizeColumn(i);
            }

            hsswb.write(fos);
            fos.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File exportXlsDepensesRealisees(List<data.DepenseAgregate> depensesRealisees) {
        FileOutputStream fos = null;
        try {
            String path = MainUI.cPath("/Media/reports");
            File file = new File(
                    path + "/ksf-depenses_realisees_" + Constants.TIMESTAMPED_FORMAT.format(new Date()) + ".xlsx");
            fos = new FileOutputStream(file);
            XSSFWorkbook workbook = new XSSFWorkbook();
            Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.DARK_BLUE.index);
            headerFont.setBold(true);

            XSSFSheet feuil = workbook.createSheet("Dépenses Réalisées");
            feuil.setColumnWidth(1, 25 * 300);
            feuil.setColumnWidth(2, 25 * 300);

            CellStyle headerCellStyle = feuil.getWorkbook().createCellStyle();
            headerCellStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.index);
            headerCellStyle.setFillPattern(FillPatternType.DIAMONDS);
            headerCellStyle.setFont(headerFont);

            int rowid = 0;
            XSSFRow row0 = feuil.createRow(rowid++);

            XSSFCell dateT = row0.createCell(0);
            dateT.setCellStyle(headerCellStyle);
            dateT.setCellValue("DATE");

            XSSFCell ref = row0.createCell(1);
            ref.setCellStyle(headerCellStyle);
            ref.setCellValue("IMPUTATION");

            XSSFCell cat = row0.createCell(2);
            cat.setCellStyle(headerCellStyle);
            cat.setCellValue("NOM DE LA DEPENSE");

            XSSFCell usd = row0.createCell(3);
            usd.setCellStyle(headerCellStyle);
            usd.setCellValue("MONTANT USD");

            XSSFCell cdf = row0.createCell(4);
            cdf.setCellStyle(headerCellStyle);
            cdf.setCellValue("MONTANT CDF");

            double sumCdf = 0, sumUsd = 0;
            for (data.DepenseAgregate dep : depensesRealisees) {
                row0 = feuil.createRow(rowid++);
                row0.createCell(0).setCellValue(
                        Constants.DATE_HEURE_USER_READABLE_FORMAT.format(java.sql.Timestamp.valueOf(dep.getDate())));
                row0.createCell(1).setCellValue(dep.getImputation());
                row0.createCell(2).setCellValue(dep.getDepenseId() != null ? dep.getDepenseId().getNomDepense() : "");
                row0.createCell(3).setCellValue(dep.getMontantUsd());
                row0.createCell(4).setCellValue(dep.getMontantCdf() == null ? 0.0 : dep.getMontantCdf());

                sumUsd += dep.getMontantUsd();
                sumCdf += (dep.getMontantCdf() == null ? 0.0 : dep.getMontantCdf());
            }

            row0 = feuil.createRow(rowid++);
            row0.createCell(2).setCellValue("TOTAL");
            row0.createCell(3).setCellValue(sumUsd);
            row0.createCell(4).setCellValue(sumCdf);

            workbook.write(fos);
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }

    public static File exportXlsAgregatedDepenses(Map<String, double[]> aggregatedData) {
        try {
            String path = MainUI.cPath("/Media/autres");
            File file = new File(
                    path + "/ksf-DepensesAgregees" + Constants.TIMESTAMPED_FORMAT.format(new Date()) + ".xlsx");
            try (Workbook workbook = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(file)) {
                Sheet sheet = workbook.createSheet("Depenses Agregees");
                CellStyle headerCellStyle = workbook.createCellStyle();
                headerCellStyle.setFillForegroundColor(
                        new XSSFColor(new java.awt.Color(0x44, 0xce, 0xf5), new DefaultIndexedColorMap()));
                headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerFont.setColor(IndexedColors.WHITE.getIndex());
                headerCellStyle.setFont(headerFont);

                Row header = sheet.createRow(0);
                String[] columns = { "CATEGORIE DE DEPENSE", "TOTAL USD", "TOTAL CDF" };
                for (int i = 0; i < columns.length; i++) {
                    Cell cell = header.createCell(i);
                    cell.setCellValue(columns[i]);
                    cell.setCellStyle(headerCellStyle);
                }

                int rowid = 1;
                double totalUsd = 0d;
                double totalCdf = 0d;
                List<Map.Entry<String, double[]>> entries = new ArrayList<>(aggregatedData.entrySet());
                entries.sort(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER));
                for (Map.Entry<String, double[]> entry : entries) {
                    double[] values = entry.getValue() == null ? new double[] { 0d, 0d } : entry.getValue();
                    Row row = sheet.createRow(rowid++);
                    row.createCell(0).setCellValue(entry.getKey());
                    row.createCell(1).setCellValue(values[0]);
                    row.createCell(2).setCellValue(values[1]);
                    totalUsd += values[0];
                    totalCdf += values[1];
                }

                Row totalRow = sheet.createRow(rowid);
                Cell label = totalRow.createCell(0);
                label.setCellValue("TOTAL");
                label.setCellStyle(headerCellStyle);
                Cell usd = totalRow.createCell(1);
                usd.setCellValue(totalUsd);
                usd.setCellStyle(headerCellStyle);
                Cell cdf = totalRow.createCell(2);
                cdf.setCellValue(totalCdf);
                cdf.setCellStyle(headerCellStyle);

                for (int i = 0; i < columns.length; i++) {
                    sheet.autoSizeColumn(i);
                }
                workbook.write(fos);
            }
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Livraison> searchLivraison(List<Livraison> livraisons, String critere) {
        List<Livraison> result = new ArrayList<>();
        for (Livraison livr : livraisons) {
            String value = (Constants.DATE_HEURE_FORMAT.format(new Date()) + " " + livr.getLibelle() + " "
                    + livr.getNumPiece() + " "
                    + "" + livr.getObservation() + " " + livr.getReference()
                    + " " + livr.getFournId().getNomFourn() + " " + livr.getFournId().getPhone());
            if (value.toUpperCase().contains(critere.toUpperCase())) {
                result.add(livr);
            }
        }
        return result;
    }

    public static File exportXlsLivraison(List<Traisorerie> lts, List<Livraison> livraisons, double taux) {
        FileOutputStream fos;
        try {
            String path = MainUI.cPath("/Media/autres");
            File file = new File(path + "/ksf-Livraisons" + Constants.TIMESTAMPED_FORMAT.format(new Date()) + ".xls");
            fos = new FileOutputStream(file);
            HSSFWorkbook hsswb = new HSSFWorkbook();
            // creation de la feuil
            HSSFSheet feuil = hsswb.createSheet("Livraison");
            int rowid = 0;
            // les entetes des colones
            HSSFRow row0 = feuil.createRow(rowid);
            Cell codebar = row0.createCell(0);
            codebar.setCellValue("DATE");
            Cell nom_fournisseur = row0.createCell(1);
            nom_fournisseur.setCellValue("FOURNISSEUR");
            Cell phones = row0.createCell(2);
            phones.setCellValue("TELEPHONES");
            Cell piece_joint = row0.createCell(3);
            piece_joint.setCellValue("PIECE_JUST");
            Cell prix_topay = row0.createCell(4);
            prix_topay.setCellValue("PRIX FOURNISSEUR");
            Cell reduct = row0.createCell(5);
            reduct.setCellValue("REDUCTION");
            Cell payed = row0.createCell(6);
            payed.setCellValue("PAYE");
            Cell restant = row0.createCell(7);
            restant.setCellValue("RESTANT");
            Cell receive = row0.createCell(8);
            receive.setCellValue("A RECEVOIR");
            Cell libelle = row0.createCell(9);
            libelle.setCellValue("LIBELLE");
            Cell observation = row0.createCell(10);
            observation.setCellValue("OBSERVATION");
            int r = 1;

            double sumtopay = 0;
            double sumreduction = 0;
            double sumpayed = 0;
            double sumremained = 0;
            double sum2receive = 0;
            double given = 0;

            for (Livraison ii : livraisons) {
                row0 = feuil.createRow(++rowid);
                Cell codebar1 = row0.createCell(0);
                codebar1.setCellValue(Constants.USER_READABLE_FORMAT.format(ii.getDateLivr()));
                Cell fourn = row0.createCell(1);
                fourn.setCellValue(ii.getFournId().getNomFourn());
                Cell phone = row0.createCell(2);
                phone.setCellValue(ii.getFournId().getPhone());
                Cell piece = row0.createCell(3);
                piece.setCellValue(ii.getNumPiece());
                Cell prix_fourn = row0.createCell(4);
                prix_fourn.setCellValue(ii.getTopay() == null ? 0 : ii.getTopay());
                sumtopay += ii.getTopay() == null ? 0 : ii.getTopay();
                Cell reduction = row0.createCell(5);
                reduction.setCellValue(ii.getReduction() == null ? 0 : ii.getReduction());
                sumreduction += ii.getReduction() == null ? 0 : ii.getReduction();
                Cell payedv = row0.createCell(6);
                payedv.setCellValue(ii.getPayed() == null ? 0 : ii.getPayed());
                sumpayed += ii.getPayed() == null ? 0 : ii.getPayed();
                Cell restantv = row0.createCell(7);
                restantv.setCellValue(ii.getRemained() == null ? 0 : ii.getRemained());
                sumremained += ii.getRemained() == null ? 0 : ii.getRemained();
                Cell arecevoir = row0.createCell(8);
                arecevoir.setCellValue(ii.getToreceive() == null ? 0 : ii.getToreceive());
                sum2receive += ii.getToreceive() == null ? 0 : ii.getToreceive();
                Cell libellev = row0.createCell(9);
                libellev.setCellValue(ii.getLibelle());
                Cell observationv = row0.createCell(10);
                observationv.setCellValue(ii.getObservation());
                given += sumify(findTresoreries(lts, Mouvment.DIMINUTION.name(), ii.getReference()), taux);
                r++;
            }
            double collected = (sumpayed + given);
            double remf = ((sumtopay - sumreduction) - collected);
            row0 = feuil.createRow(r);
            Cell conc1 = row0.createCell(0);
            conc1.setCellValue("TOTAL ");
            Cell conc2 = row0.createCell(4);
            conc2.setCellValue(sumtopay);
            Cell conc3 = row0.createCell(5);
            conc3.setCellValue(sumreduction);
            Cell conc4 = row0.createCell(6);
            conc4.setCellValue(sumpayed);
            Cell conc5 = row0.createCell(7);
            conc5.setCellValue(sumremained);
            Cell conc6 = row0.createCell(8);
            conc6.setCellValue(sum2receive);

            row0 = feuil.createRow(++r);
            Cell conc12 = row0.createCell(0);
            conc12.setCellValue("DECAISSEMENT");
            Cell conc42 = row0.createCell(6);
            conc42.setCellValue(given);

            row0 = feuil.createRow(++r);
            Cell conc13 = row0.createCell(0);
            conc13.setCellValue("TOTAUX ");
            Cell conc23 = row0.createCell(4);
            conc23.setCellValue(sumtopay);
            Cell conc33 = row0.createCell(5);
            conc33.setCellValue(sumreduction);
            Cell conc43 = row0.createCell(6);
            conc43.setCellValue(collected);
            Cell conc53 = row0.createCell(7);
            conc53.setCellValue(remf);
            Cell conc63 = row0.createCell(8);
            conc63.setCellValue(sum2receive);

            hsswb.write(fos);
            fos.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static double sumify(List<Traisorerie> listr, double taux) {
        double result = 0;
        for (Traisorerie tr : listr) {
            result += (tr.getMontantUsd() + (tr.getMontantCdf() / taux));
        }
        return result;
    }

    public static File exportXlsSales(List<TreeItem<SaleItem>> listSales) {
        try {
            String path = MainUI.cPath("/Media/autres");
            File file = new File(path + "/ksf-ventes_" + Constants.TIMESTAMPED_FORMAT.format(new Date()) + ".xlsx");
            try (Workbook workbook = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(file)) {
                Sheet sheet = workbook.createSheet("ventes");
                CellStyle headerCellStyle = workbook.createCellStyle();
                headerCellStyle.setFillForegroundColor(
                        new XSSFColor(new java.awt.Color(0x44, 0xce, 0xf5), new DefaultIndexedColorMap()));
                headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerFont.setColor(IndexedColors.WHITE.getIndex());
                headerCellStyle.setFont(headerFont);

                Row header = sheet.createRow(0);
                String[] columns = { "Date", "Factures", "Details/Nombre d'articles", "Cash USD", "Cash CDF", "Dette",
                        "Echeance", "Client" };
                for (int i = 0; i < columns.length; i++) {
                    Cell cell = header.createCell(i);
                    cell.setCellValue(columns[i]);
                    cell.setCellStyle(headerCellStyle);
                }

                double totusd = 0, totcdf = 0;
                int r = 1;
                for (TreeItem<SaleItem> dayNode : listSales) {
                    if (dayNode == null || dayNode.getValue() == null) {
                        continue;
                    }
                    SaleItem day = dayNode.getValue();
                    Row dayRow = sheet.createRow(r++);
                    dayRow.createCell(0)
                            .setCellValue(day.getDateDeVente() == null ? "" : day.getDateDeVente().toString());
                    dayRow.createCell(1).setCellValue(day.getFacture() == null ? "" : day.getFacture());
                    dayRow.createCell(2).setCellValue("SOUS TOTAUX : ");
                    dayRow.createCell(3).setCellValue(day.getSaleAmountUsd());
                    dayRow.createCell(4).setCellValue(day.getSaleAmountCdf());
                    dayRow.createCell(5).setCellValue(day.getSaleAmountCredit());
                    totusd += day.getSaleAmountUsd();
                    totcdf += day.getSaleAmountCdf();

                    if (!dayNode.isExpanded()) {
                        continue;
                    }
                    for (TreeItem<SaleItem> saleNode : dayNode.getChildren()) {
                        if (saleNode == null || saleNode.getValue() == null) {
                            continue;
                        }
                        SaleItem sale = saleNode.getValue();
                        Row saleRow = sheet.createRow(r++);
                        saleRow.createCell(0).setCellValue(sale.getDateHeureVente() == null ? ""
                                : Constants.DATE_HEURE_USER_READABLE_FORMAT.format(sale.getDateHeureVente()));
                        saleRow.createCell(1).setCellValue(sale.getFacture() == null ? "" : sale.getFacture());
                        saleRow.createCell(2).setCellValue(sale.getProduitName() == null ? "" : sale.getProduitName());
                        saleRow.createCell(3).setCellValue(sale.getSaleAmountUsd());
                        saleRow.createCell(4).setCellValue(sale.getSaleAmountCdf());
                        saleRow.createCell(5).setCellValue(sale.getSaleAmountCredit());
                        saleRow.createCell(6)
                                .setCellValue(sale.getDatEcheance() == null ? "" : sale.getDatEcheance().toString());
                        saleRow.createCell(7).setCellValue(
                                (sale.getClient() != null && sale.getClient().getPhone() != null)
                                        ? sale.getClient().getPhone()
                                        : "");

                        if (!saleNode.isExpanded()) {
                            continue;
                        }
                        for (TreeItem<SaleItem> lineNode : saleNode.getChildren()) {
                            if (lineNode == null || lineNode.getValue() == null) {
                                continue;
                            }
                            SaleItem line = lineNode.getValue();
                            Row lineRow = sheet.createRow(r++);
                            lineRow.createCell(2).setCellValue(
                                    line.getQuantite() + " " + (line.getMesure() == null ? "" : line.getMesure()) + " "
                                            + (line.getProduitName() == null ? "" : line.getProduitName()));
                            lineRow.createCell(3).setCellValue(line.getSaleAmountUsd());
                            lineRow.createCell(4).setCellValue(line.getSaleAmountCdf());
                            lineRow.createCell(1).setCellValue(line.getFacture() == null ? "" : line.getFacture());
                        }
                    }
                }

                Row totalRow = sheet.createRow(r);
                Cell totalLabel = totalRow.createCell(0);
                totalLabel.setCellValue("TOTAUX");
                totalLabel.setCellStyle(headerCellStyle);
                Cell totalUsd = totalRow.createCell(3);
                totalUsd.setCellValue(totusd);
                totalUsd.setCellStyle(headerCellStyle);
                Cell totalCdf = totalRow.createCell(4);
                totalCdf.setCellValue(totcdf);
                totalCdf.setCellStyle(headerCellStyle);

                for (int i = 0; i < columns.length; i++) {
                    sheet.autoSizeColumn(i);
                }
                workbook.write(fos);
            }
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File exportXlsReports(List<List<ChartItem>> itemss, String criteria, String time) {

        FileOutputStream fos;
        try {
            String path = MainUI.cPath("/Media/autres");
            File file = new File(path + "/ksf-Rapport_" + criteria.replace(" ", "-") + "_"
                    + Constants.TIMESTAMPED_FORMAT.format(new Date()) + ".xls");
            fos = new FileOutputStream(file);
            HSSFWorkbook hsswb = new HSSFWorkbook();
            // creation de la feuil
            HSSFSheet feuil = hsswb.createSheet(criteria + "-" + time);
            int rowid = 0;
            int col = 1;
            HSSFRow row0 = feuil.createRow(rowid);
            double charge = 0;
            List<String> absices = mergeAbsices(itemss, time);
            for (List<ChartItem> elements : itemss) {
                // headers
                if (elements.isEmpty()) {
                    continue;
                }

                List<ChartItem> items = getTimeGrouped(elements, time);
                col++;
                ChartItem sample = items.get(0);
                if (col == 2) {
                    row0 = feuil.createRow(rowid);
                } else {
                    row0 = feuil.getRow(rowid);
                }
                Cell date = row0.createCell(0);
                date.setCellValue("Date");
                Cell periode = row0.createCell(1);
                periode.setCellValue("Periode");
                Cell montant = row0.createCell(col);
                if (!sample.getSerieName().equals("Marge")) {
                    montant.setCellValue(sample.getSerieName());
                }
                System.out.println("Series " + sample.getSerieName());
                double sum = 0;
                double sv = 0;
                for (String absice : absices) {
                    ChartItem item = findItemByAbsice(items, absice);
                    int rowId = ++rowid;
                    if (item != null) {
                        if (col == 2) {
                            row0 = feuil.createRow(rowId);
                        } else {
                            row0 = feuil.getRow(rowId);
                        }
                        Cell date1 = row0.createCell(0);
                        date1.setCellValue(Constants.USER_READABLE_FORMAT.format(item.getDate()));
                        Cell ref = row0.createCell(1);
                        ref.setCellValue(absice);
                        Cell amount = row0.createCell(col);
                        if (!sample.getSerieName().equals("Marge")) {
                            amount.setCellValue(item.getAmmount());
                        }
                        sum += item.getAmmount();

                    } else {
                        if (col == 2) {
                            row0 = feuil.createRow(rowId);
                        } else {
                            row0 = feuil.getRow(rowId);
                        }
                        Cell date1 = row0.createCell(0);
                        if (absice.contains(" ")) {
                            String dat = absice.split(" ")[0];
                            String data[] = dat.split("-");
                            Collections.reverse(Arrays.asList(data));
                            String joined = StringUtil.join(data, "/");
                            date1.setCellValue(joined);
                        } else {
                            if (absice.contains("-")) {
                                String data[] = absice.split("-");
                                Collections.reverse(Arrays.asList(data));
                                String joined = StringUtil.join(data, "/");
                                date1.setCellValue(joined);
                            } else {
                                date1.setCellValue(absice);
                            }
                        }
                        Cell ref = row0.createCell(1);
                        ref.setCellValue(absice);
                        Cell amount = row0.createCell(col);
                        if (!sample.getSerieName().equals("Marge")) {
                            amount.setCellValue(0);
                            sum += 0;
                        }

                    }
                }

                if (col == 2) {
                    row0 = feuil.createRow(++rowid);
                } else {
                    row0 = feuil.getRow(++rowid);
                }
                Cell c = row0.createCell(0);
                c.setCellValue("TOTAL");
                Cell c1 = row0.createCell(col);
                if (!sample.getSerieName().equals("Marge")) {
                    c1.setCellValue(sum);
                } else {

                    Double v1 = row0.getCell(col - 1).getNumericCellValue();
                    HSSFCell cell = row0.getCell(col - 2);
                    Double v2 = cell == null ? 0 : cell.getNumericCellValue();
                    double mrg = v1 - v2;
                    row0 = feuil.createRow(++rowid);
                    Cell c2 = row0.createCell(0);
                    c2.setCellValue("MARGE");
                    Cell c3 = row0.createCell(2);
                    c3.setCellValue(mrg);
                }
                rowid = 0;
            }
            hsswb.write(fos);
            fos.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File exportXlsSalePerProductReports(List<SaleReport> items, Periode periode) {

        FileOutputStream fos;
        try {
            String path = MainUI.cPath("/Media/autres");
            File file = new File(
                    path + "/ksf-Rapport_sales_products_" + Constants.TIMESTAMPED_FORMAT.format(new Date()) + ".xls");
            fos = new FileOutputStream(file);
            HSSFWorkbook hsswb = new HSSFWorkbook();
            // creation de la feuil
            HSSFSheet feuil = hsswb.createSheet("Sales");
            int rowid = 0;
            double sum = 0, ben = 0, cv = 0;
            HSSFRow row_1 = feuil.createRow(rowid);
            row_1.createCell(0).setCellValue("Rapport de " + periode.getComment() + "/" + periode.getRegion() + ""
                    + " allant du " + periode.getDateDebut().toString()
                    + " au " + periode.getDateFin().toString());
            row_1.createCell(2).setCellValue("Fait a " + periode.getRegion() + ", le " + LocalDate.now());
            rowid += 2;
            HSSFRow row0 = feuil.createRow(rowid);
            Cell date = row0.createCell(0);
            date.setCellValue("Codebar");
            Cell produit = row0.createCell(1);
            produit.setCellValue("Product");
            Cell q = row0.createCell(2);
            q.setCellValue("Quatite");
            Cell u = row0.createCell(3);
            u.setCellValue("Unite");
            Cell ventes = row0.createCell(4);
            ventes.setCellValue("Ventes");
            Cell cout = row0.createCell(5);
            cout.setCellValue("Cout-Achat");
            Cell marge = row0.createCell(6);
            marge.setCellValue("Marge");
            Cell montant1 = row0.createCell(7);
            montant1.setCellValue("Pourcentage");

            for (SaleReport item : items) {
                // headers
                rowid++;
                row0 = feuil.createRow(rowid);
                row0.createCell(0).setCellValue(item.codebar());
                row0.createCell(1).setCellValue(item.produit());
                row0.createCell(2).setCellValue(item.quantite());
                row0.createCell(3).setCellValue(item.unite());
                row0.createCell(4).setCellValue(item.vente());
                row0.createCell(5).setCellValue(item.coutAchat());
                row0.createCell(6).setCellValue(item.marge());
                double pr = item.percentMarge();
                double perc = BigDecimal.valueOf(pr).setScale(1, RoundingMode.HALF_EVEN).doubleValue();
                row0.createCell(7).setCellValue((perc) + "%");
                sum += item.vente();
                ben += item.marge();
                cv += item.coutAchat();
            }
            rowid++;
            rowid++;
            row0 = feuil.createRow(rowid);
            Cell date1 = row0.createCell(0);
            date1.setCellValue("TOTAUX");

            Cell produit1 = row0.createCell(1);
            produit1.setCellValue(" - ");
            row0.createCell(4).setCellValue(sum);
            row0.createCell(5).setCellValue(cv);
            row0.createCell(6).setCellValue(ben);
            row0.createCell(7).setCellValue("100%");
            hsswb.write(fos);
            fos.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File exportXlsSalePerCategory(List<SaleReport> items) {

        FileOutputStream fos;
        try {
            String path = MainUI.cPath("/Media/autres");
            File file = new File(path + "/ksf-Rapport_sales_per_Category_"
                    + Constants.TIMESTAMPED_FORMAT.format(new Date()) + ".xls");
            fos = new FileOutputStream(file);
            HSSFWorkbook hsswb = new HSSFWorkbook();
            // creation de la feuil
            HSSFSheet feuil = hsswb.createSheet("Sales");
            int rowid = 0;
            double sum = 0;
            HSSFRow row0 = feuil.createRow(rowid);
            Cell date = row0.createCell(0);
            date.setCellValue("Category");
            Cell montant = row0.createCell(1);
            montant.setCellValue("Amount");
            Cell percent = row0.createCell(2);
            percent.setCellValue("Percentage");
            for (SaleReport item : items) {
                // headers
                rowid++;
                row0 = feuil.createRow(rowid);
                Cell date1 = row0.createCell(0);
                date1.setCellValue(item.category());
                Cell montant1 = row0.createCell(1);
                double mon = item.vente();
                montant1.setCellValue(mon);
                Cell percent1 = row0.createCell(2);
                double pr = mon / items.stream().mapToDouble(s -> s.vente()).sum();
                double perc = BigDecimal.valueOf((pr * 100)).setScale(1, RoundingMode.HALF_EVEN).doubleValue();
                percent1.setCellValue(perc + "%");
                sum += item.vente();
            }
            rowid++;
            row0 = feuil.createRow(rowid);
            Cell date1 = row0.createCell(0);
            date1.setCellValue("TOTAL USD");

            Cell produit1 = row0.createCell(1);
            produit1.setCellValue(" - ");
            Cell montant1 = row0.createCell(2);
            montant1.setCellValue(sum);
            hsswb.write(fos);
            fos.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File exportXlsSalePerClient(List<VenteReporter> items, ResourceBundle bundle) {

        FileOutputStream fos;
        try {
            String path = MainUI.cPath("/Media/autres");
            File file = new File(
                    path + "/ksf-Rapport_sales_per_Client_" + Constants.TIMESTAMPED_FORMAT.format(new Date()) + ".xls");
            fos = new FileOutputStream(file);
            HSSFWorkbook hsswb = new HSSFWorkbook();
            // creation de la feuil
            HSSFSheet feuil = hsswb.createSheet("Sales");
            int rowid = 0;
            double sum = 0;
            HSSFRow row0 = feuil.createRow(rowid);
            Cell date = row0.createCell(0);
            date.setCellValue("Phones");
            Cell produit = row0.createCell(1);
            produit.setCellValue("Client names");
            Cell q = row0.createCell(2);
            q.setCellValue("Category");
            Cell montant = row0.createCell(3);
            montant.setCellValue("Amount");
            Cell percent = row0.createCell(4);
            percent.setCellValue("Percentage");
            for (VenteReporter item : items) {
                // headers
                rowid++;
                row0 = feuil.createRow(rowid);
                Cell date1 = row0.createCell(0);
                Client c = item.getClient();
                date1.setCellValue(c.getPhone());
                Cell produit1 = row0.createCell(1);
                produit1.setCellValue(c.getNomClient());
                String typecli = c.getTypeClient().equals("#0") ? bundle.getString("consumer")
                        : c.getTypeClient().equals("#1") ? bundle.getString("wholesaler")
                                : c.getTypeClient().equals("#2") ? bundle.getString("detailor")
                                        : c.getTypeClient().equals("#3") ? bundle.getString("subscriber")
                                                : bundle.getString("consumer");
                Cell q1 = row0.createCell(2);
                q1.setCellValue(typecli);
                Cell montant1 = row0.createCell(3);
                double mon = item.getChiffre();
                montant1.setCellValue(mon);
                Cell percent1 = row0.createCell(4);
                double pr = mon / item.getSommeTotal();
                double perc = BigDecimal.valueOf(pr * 100).setScale(1, RoundingMode.HALF_EVEN).doubleValue();
                percent1.setCellValue(perc + "%");
                sum += item.getChiffre();
            }
            rowid++;
            row0 = feuil.createRow(rowid);
            Cell date1 = row0.createCell(0);
            date1.setCellValue("TOTAL USD");

            Cell produit1 = row0.createCell(1);
            produit1.setCellValue(" - ");
            Cell montant1 = row0.createCell(2);
            montant1.setCellValue(sum);
            hsswb.write(fos);
            fos.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> double sumUp(List<T> data, boolean forSaleCredit, double taux) {
        double result = 0;
        for (T t : data) {
            if (t instanceof Vente) {
                Vente v = (Vente) t;
                double c = 0;
                if (forSaleCredit) {
                    c = v.getMontantDette() == null ? 0 : v.getMontantDette();
                    result += c;
                } else {
                    c = (v.getMontantCdf() / taux) + v.getMontantUsd();
                    result += c;
                }
            } else if (t instanceof Operation) {
                Operation ops = (Operation) t;
                double c = (ops.getMontantCdf() / taux) + ops.getMontantUsd();
                result += c;
            } else if (t instanceof Traisorerie) {
                Traisorerie trz = (Traisorerie) t;
                double c = (trz.getMontantCdf() / taux) + trz.getMontantUsd();
                result += c;
            } else if (t instanceof Stocker) {
                Stocker stocker = (Stocker) t;
                result += stocker.getPrixAchatTotal();
            } else if (t instanceof Livraison) {
                Livraison l = (Livraison) t;
                result += l.getTopay();
            }
        }
        return result;
    }

    public static double[] getTotLivr_Topay_Payed_andRemained(List<Livraison> livrz) {
        double rst[] = new double[3];
        double topay = 0, payed = 0, rem = 0;
        for (Livraison livr : livrz) {
            topay += livr.getTopay();
            payed += livr.getPayed() == null ? 0 : livr.getPayed();
            rem += livr.getRemained() == null ? 0 : livr.getRemained();
        }
        rst[0] = topay;
        rst[1] = payed;
        rst[2] = rem;
        return rst;
    }

    public static double sumAllCurency(List<Traisorerie> lts, double taux) {
        double rst = 0;
        for (Traisorerie t : lts) {
            rst += (t.getMontantUsd() + (t.getMontantCdf() / taux));
        }
        return rst;
    }

    public static List<Traisorerie> collectPaidDebt(List<Traisorerie> lt) {
        List<Traisorerie> result = new ArrayList<>();
        for (Traisorerie t : lt) {
            String libelle = t.getLibelle();
            if (libelle == null) {
                continue;
            }
            if (libelle.startsWith("Recouvrement dette ")) {
                result.add(t);
            }
        }
        return result;
    }

    public static List<Traisorerie> collectPaidDebt(List<Traisorerie> lt, String region) {
        List<Traisorerie> result = new ArrayList<>();
        for (Traisorerie t : lt) {
            if (t.getRegion() == null) {
                continue;
            }
            if (t.getLibelle().startsWith("Recouvrement dette ") && t.getRegion().equals(region)) {
                result.add(t);
            }
        }
        return result;
    }

    public static List<Traisorerie> getPayment(List<Traisorerie> lt, String facture) {
        List<Traisorerie> result = new ArrayList<>();
        for (Traisorerie t : lt) {
            if (t.getLibelle().startsWith("Recouvrement dette " + facture)) {
                result.add(t);
            }
        }
        return result;
    }

    public static List<Vente> getVenteCredits(List<Vente> lt, String ref) {
        List<Vente> result = new ArrayList<>();
        for (Vente t : lt) {
            if (t.getMontantDette() != null) {
                if (t.getMontantDette() > 0 && t.getReference().equals(ref)) {
                    result.add(t);
                }
            }
        }
        return result;
    }

    public static List<Vente> getVenteForRegion(List<Vente> lt, String reg) {
        List<Vente> result = new ArrayList<>();
        for (Vente t : lt) {
            if (t.getRegion() == null) {
                continue;
            }
            if (reg != null) {
                if (t.getRegion().equals(reg)) {
                    result.add(t);
                }
            } else {
                result.add(t);
            }
        }
        return result;
    }

    public static List<Vente> getVenteCredits(List<Vente> lt, String ref, String region) {
        List<Vente> result = new ArrayList<>();
        for (Vente t : lt) {
            if (t.getMontantDette() != null) {
                if (t.getRegion() == null) {
                    continue;
                }
                if (t.getMontantDette() > 0 && t.getReference().equals(ref) && t.getRegion().equals(region)) {
                    result.add(t);
                }
            }
        }
        return result;
    }

    public static List<DebtItem> getDebts(List<Vente> vts, List<Traisorerie> lts, double taux) {
        List<DebtItem> result = new ArrayList<>();
        for (Vente v : vts) {
            if (v.getMontantDette() != null) {
                if (v.getMontantDette() > 0) {
                    DebtItem item = new DebtItem();
                    List<Traisorerie> lpy = getPayment(lts, v.getReference());
                    double paid = sumUp(lpy, false, taux);
                    double reste = v.getMontantDette() - paid;
                    item.setDate(v.getDateVente().toLocalDate());
                    item.setFacture(v.getReference());
                    item.setMontantDette(v.getMontantDette());
                    item.setMontantPaye(paid);
                    item.setMontantRestant(reste);
                    item.setNomClient(v.getClientId().getNomClient());
                    item.setPhoneClient(v.getClientId().getPhone());
                    result.add(item);
                }
            }
        }
        return result;
    }

    public static List<List<ChartItem>> arrange(List<String> dates, List<List<ChartItem>> data) {

        List<List<ChartItem>> result = new ArrayList<>();
        List<ChartItem> inputs = new ArrayList<>();
        List<ChartItem> outputs = new ArrayList<>();
        List<ChartItem> diff = new ArrayList<>();
        List<ChartItem> inputd = data.get(1);
        List<ChartItem> outputd = data.get(0);
        List<ChartItem> diffd = data.get(2);
        for (String date : dates) {
            ChartItem out = findItemByAbsice(outputd, date);
            ChartItem in = findItemByAbsice(inputd, date);
            ChartItem dif = findItemByAbsice(diffd, date);
            if (out != null) {
                outputs.add(out);
            } else {
                ChartItem ci = new ChartItem();
                ci.setAbsices(date);
                ci.setAmmount(0);
                ci.setDate(LocalDate.parse(date));
                ci.setSerieName("Depenses");
                outputs.add(ci);
            }
            if (in != null) {
                inputs.add(in);
            } else {
                ChartItem ci = new ChartItem();
                ci.setAbsices(date);
                ci.setAmmount(0);
                ci.setDate(LocalDate.parse(date));
                ci.setSerieName("Ventes");
                inputs.add(ci);
            }
            if (dif != null) {
                diff.add(dif);
            } else {
                ChartItem ci = new ChartItem();
                ci.setAbsices(date);
                ci.setAmmount(0);
                ci.setDate(LocalDate.parse(date));
                ci.setSerieName("Marge");
                diff.add(ci);
            }
        }
        result.add(outputs);
        result.add(inputs);
        result.add(diff);
        return result;
    }

    public static List<String> mergeAndSort(List<List<ChartItem>> dataseries) {
        List<String> result = new ArrayList<>();
        List<ChartItem> t3 = new ArrayList<>();
        for (List<ChartItem> dataset : dataseries) {
            for (ChartItem t : dataset) {
                ChartItem abs = findItemByAbsice(dataset, t.getAbsices());
                if (abs != null) {
                    t3.add(abs);
                }

            }
        }
        Collections.sort(t3, new ChartItem());
        for (ChartItem i : t3) {
            if (!result.contains(i.getAbsices())) {
                result.add(i.getAbsices());
            }
        }
        return result;
    }

    public static <T> List<String> mergeAbsices(List<List<T>> dataseries, String time) {
        List<String> result = new ArrayList<>();
        List<ChartItem> t3 = new ArrayList<>();
        if (time.equals("Par mois")) {
            for (List<T> dataset : dataseries) {
                for (T t : dataset) {
                    if (t instanceof ChartItem) {
                        ChartItem item = (ChartItem) t;
                        if (!result.contains(item.getAbsices())) {
                            t3.add(item);
                        }
                    }
                }
            }
            List<ChartItem> ms = sortByMonth(t3);
            for (ChartItem item : ms) {
                if (!result.contains(item.getAbsices())) {
                    result.add(item.getAbsices());
                }
            }

        } else if (time.equals("Par jours")) {
            for (List<T> dataset : dataseries) {
                for (T t : dataset) {
                    if (t instanceof ChartItem) {
                        ChartItem item = (ChartItem) t;
                        if (!result.contains(item.getAbsices())) {
                            t3.add(item);
                        }
                    }
                }
            }
            List<ChartItem> ms = getTimeGrouped(t3, time);
            for (ChartItem item : ms) {
                if (!result.contains(item.getAbsices())) {
                    result.add(item.getAbsices());
                }
            }
        } else if (time.equals("Par année")) {
            for (List<T> dataset : dataseries) {
                for (T t : dataset) {
                    if (t instanceof ChartItem) {
                        ChartItem item = (ChartItem) t;
                        if (!result.contains(item.getAbsices())) {
                            t3.add(item);
                        }
                    }
                }
            }
            List<ChartItem> ms = sortByYear(t3);
            for (ChartItem item : ms) {
                if (!result.contains(item.getAbsices())) {
                    result.add(item.getAbsices());
                }
            }
        } else {
            for (List<T> dataset : dataseries) {
                for (T t : dataset) {
                    if (t instanceof ChartItem) {
                        ChartItem item = (ChartItem) t;
                        if (!result.contains(item.getAbsices())) {
                            result.add(item.getAbsices());
                        }
                    }
                }
            }
        }
        return result;
    }

    public static List<ChartItem> sortByMonth(List<ChartItem> ls) {
        List<ChartItem> result = new ArrayList<>();
        for (ChartItem ci : ls) {
            ChartItem or = new ChartItem();
            or.setSerieName(ci.getSerieName());
            String dt = ci.getAbsices().split(" ")[0];
            if (dt.contains("-")) {
                dt = dt.split("-")[0] + "-" + dt.split("-")[1];
            }
            or.setAbsices(dt);
            double sum = 0;
            for (ChartItem cx : ls) {
                if (cx.getAbsices().contains(dt)) {
                    sum += cx.getAmmount();
                }
            }
            or.setAmmount(sum);
            or.setDate(ci.getDate());
            result.add(or);
        }
        return result;
    }

    public static List<ChartItem> sortByYear(List<ChartItem> ls) {
        List<ChartItem> result = new ArrayList<>();
        for (ChartItem ci : ls) {
            ChartItem or = new ChartItem();
            or.setSerieName(ci.getSerieName());
            String dt = ci.getAbsices().split(" ")[0].split("-")[0];
            or.setAbsices(dt);
            double sum = 0;
            for (ChartItem cx : ls) {
                if (cx.getAbsices().startsWith(dt)) {
                    sum += cx.getAmmount();
                }
            }
            or.setAmmount(sum);
            or.setDate(ci.getDate());
            result.add(or);
        }
        return result;
    }

    private static double sumItems(List<ChartItem> lci, String q) {
        double sum = 0;
        for (ChartItem c : lci) {
            if (c.getAbsices().contains(q)) {
                sum += c.getAmmount();
            }
        }
        return sum;
    }

    // public static List<SaleItem> getSaleItemWithDate(List<SaleItem> sales, String
    // date) {
    // List<SaleItem> rst = new ArrayList<>();
    // for (SaleItem s : sales) {
    // String d = Constants.DATE_HEURE_FORMAT.format(s.getDate());
    // date = date.replace("00:00:00", "");
    // if (d.toUpperCase().startsWith(date)) {
    // rst.add(s);
    // }
    // }
    // return rst;
    // }
    public static void installTooltip(Node node, String title) {
        Tooltip thome = new Tooltip();
        thome.setText(title);
        thome.setStyle("-fx-font: normal bold 14 Langdon; "
                + "-fx-base: #EEEEEE; "
                + "-fx-text-fill: white;");
        Tooltip.install(node, thome);
    }

    public static File exportXlsInventoryMagasin(HashMap<String, String> bundleData, List<InventoryMagasin> lisinvent,
            String dev) {
        FileOutputStream fos;
        try {
            String path = MainUI.cPath("/Media/inventories");
            File file = new File(path + "/ksf-inv_mag_" + Constants.TIMESTAMPED_FORMAT.format(new Date()) + ".xlsx");
            fos = new FileOutputStream(file);
            XSSFWorkbook workbook = new XSSFWorkbook();
            Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.DARK_BLUE.index);
            headerFont.setBold(true);

            Font headertitle = workbook.createFont();
            headertitle.setColor(IndexedColors.SKY_BLUE.index);
            headertitle.setBold(true);

            // creation de la feuil
            XSSFSheet feuil = workbook.createSheet("Inventaire magasin");
            feuil.setColumnWidth(1, 25 * 400);

            CellStyle headerCelltitle = feuil.getWorkbook().createCellStyle();
            headerCelltitle.setFont(headertitle);

            CellStyle headerCellStyle = feuil.getWorkbook().createCellStyle();
            // fill foreground color ...
            headerCellStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.index);
            // and solid fill pattern produces solid grey cell fill
            headerCellStyle.setFillPattern(FillPatternType.DIAMONDS);
            headerCellStyle.setFont(headerFont);
            int rowid = 0;
            // les entetes des colones
            Format df = new SimpleDateFormat("dd/MM/yyyy");
            String leo = df.format(new Date());
            XSSFRow row0 = feuil.createRow(rowid++);

            XSSFCell enttx = row0.createCell(1);
            enttx.setCellValue(bundleData.get("entrep"));

            XSSFCell enttdate = row0.createCell(10);
            enttdate.setCellValue("Fait le : " + leo);

            row0 = feuil.createRow(rowid++);
            XSSFCell enttrcm = row0.createCell(1);
            enttrcm.setCellValue(bundleData.get("rccm"));
            XSSFCell entdebut = row0.createCell(10);
            entdebut.setCellValue("Debut : " + bundleData.get("debut"));

            row0 = feuil.createRow(rowid++);
            XSSFCell enttreg = row0.createCell(1);
            enttreg.setCellValue(bundleData.get("region"));
            XSSFCell enttf = row0.createCell(10);
            enttf.setCellValue("Fin : " + bundleData.get("fin"));

            row0 = feuil.createRow(rowid++);
            XSSFCell enttop = row0.createCell(10);
            enttop.setCellValue("Operateur : " + bundleData.get("operateur"));
            row0 = feuil.createRow(rowid++);
            XSSFCell enttdev = row0.createCell(10);
            enttdev.setCellValue("Devise : " + dev);

            row0 = feuil.createRow(rowid += 2);
            XSSFCell enttvalue = row0.createCell(10);
            enttvalue.setCellValue("Valeur : ");
            XSSFCell enttvaluen = row0.createCell(11);
            double somme = 0;
            String entrep = bundleData.get("eUid");
            File f = FileUtils.pointFile(entrep + ".png");
            InputStream is;
            if (!f.exists()) {
                is = MainuiController.class.getResourceAsStream("/icons/gallery.png");
                f = FileUtils.streamTofile(is);
            }
            is = new FileInputStream(f);

            byte[] bytes = IOUtils.toByteArray(is);
            int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
            is.close();
            XSSFCreationHelper helper = workbook.getCreationHelper();
            Drawing drawing = feuil.createDrawingPatriarch();
            // add a picture shape
            XSSFClientAnchor anchor = helper.createClientAnchor();
            anchor.setCol1(0);
            anchor.setRow1(0);
            Picture pict = drawing.createPicture(anchor, pictureIdx);
            pict.resize();
            row0 = feuil.createRow(rowid += 2);
            XSSFCell enttitle = row0.createCell(5);

            enttitle.setCellValue("INVENTAIRE DE STOCK DU POINT DE VENTE ");
            enttitle.setCellStyle(headerCelltitle);
            row0 = feuil.createRow(rowid += 2);

            XSSFCell codebar = row0.createCell(0);
            codebar.setCellStyle(headerCellStyle);
            codebar.setCellValue("CODE");
            XSSFCell nom_produit = row0.createCell(1);
            nom_produit.setCellStyle(headerCellStyle);
            nom_produit.setCellValue("PRODUIT");
            XSSFCell marque = row0.createCell(2);
            marque.setCellStyle(headerCellStyle);
            marque.setCellValue("MARQUE");
            XSSFCell modele = row0.createCell(3);
            modele.setCellStyle(headerCellStyle);
            modele.setCellValue("MODELE/FORME");
            XSSFCell taille = row0.createCell(4);
            taille.setCellStyle(headerCellStyle);
            taille.setCellValue("COCENTRATION/TAILLE");
            XSSFCell lot = row0.createCell(5);
            lot.setCellStyle(headerCellStyle);
            lot.setCellValue("LOT");
            XSSFCell mesure = row0.createCell(6);
            mesure.setCellStyle(headerCellStyle);
            mesure.setCellValue("MESURE");
            XSSFCell entree = row0.createCell(7);
            entree.setCellStyle(headerCellStyle);
            entree.setCellValue("ENTREES");
            XSSFCell sortie = row0.createCell(8);
            sortie.setCellStyle(headerCellStyle);
            sortie.setCellValue("SORTIES");
            XSSFCell stock_th = row0.createCell(9);
            stock_th.setCellStyle(headerCellStyle);
            stock_th.setCellValue("STOCK THEORIQUE");
            XSSFCell stock_ph = row0.createCell(10);
            stock_ph.setCellStyle(headerCellStyle);
            stock_ph.setCellValue("STOCK PHYSIQUE");
            XSSFCell alerte = row0.createCell(11);
            alerte.setCellStyle(headerCellStyle);
            alerte.setCellValue("ALERTE");
            XSSFCell pau_usd = row0.createCell(12);
            pau_usd.setCellStyle(headerCellStyle);
            pau_usd.setCellValue("P.A. UNIT");
            XSSFCell valeur_total = row0.createCell(13);
            valeur_total.setCellStyle(headerCellStyle);
            valeur_total.setCellValue("VALEUR STOCK");
            XSSFCell local = row0.createCell(14);
            local.setCellStyle(headerCellStyle);
            local.setCellValue("LOCALISATION");
            XSSFCell date_expir = row0.createCell(15);
            date_expir.setCellStyle(headerCellStyle);
            date_expir.setCellValue("DATE EXP.");
            XSSFCell prixdevente = row0.createCell(16);
            prixdevente.setCellStyle(headerCellStyle);
            prixdevente.setCellValue("PRIX-VENTE");
            XSSFCell devise = row0.createCell(17);
            devise.setCellStyle(headerCellStyle);
            devise.setCellValue("DEVISE");
            XSSFCell multibatch = row0.createCell(18);
            multibatch.setCellStyle(headerCellStyle);
            multibatch.setCellValue("MULTI-LOT");
            for (InventoryMagasin ii : lisinvent) {
                row0 = feuil.createRow(++rowid);
                XSSFCell codebar1 = row0.createCell(0);
                codebar1.setCellValue(ii.getProduit().getCodebar());
                XSSFCell nom_prod = row0.createCell(1);
                nom_prod.setCellValue(ii.getProduit().getNomProduit());
                XSSFCell pmarque = row0.createCell(2);
                pmarque.setCellValue(ii.getProduit().getMarque());
                XSSFCell pmodele = row0.createCell(3);
                pmodele.setCellValue(ii.getProduit().getModele());
                XSSFCell ptaille = row0.createCell(4);
                ptaille.setCellValue((ii.getProduit().getTaille() == null ? "" : ii.getProduit().getTaille()));
                XSSFCell dlot = row0.createCell(5);
                dlot.setCellValue(ii.getLot());
                XSSFCell dmesure = row0.createCell(6);
                dmesure.setCellValue(ii.getMesure().getDescription() + ":" + ii.getMesure().getQuantContenu());
                XSSFCell entrees = row0.createCell(7);
                entrees.setCellValue(ii.getQuantEntree());
                XSSFCell quant_out = row0.createCell(8);
                quant_out.setCellValue(ii.getQuantSortie());
                XSSFCell quant_remain = row0.createCell(9);
                quant_remain.setCellValue(ii.getQuantStock());
                XSSFCell stock_phys = row0.createCell(10);
                stock_phys.setCellValue(0);
                XSSFCell stock_alerte = row0.createCell(11);
                stock_alerte.setCellValue(ii.getAlerte());
                XSSFCell dpau = row0.createCell(12);
                dpau.setCellValue(ii.getCoutAchat());
                XSSFCell dvaleur = row0.createCell(13);
                somme += ii.getValeurStock();
                dvaleur.setCellValue(ii.getValeurStock());
                XSSFCell loc = row0.createCell(14);
                loc.setCellValue(ii.getLocalisation());
                XSSFCell dexpir = row0.createCell(15);
                LocalDate dexp = ii.getExpiry();
                dexpir.setCellValue(dexp == null ? "" : dexp.toString());
                XSSFCell vprice = row0.createCell(16);
                vprice.setCellValue(ii.getPrixDeVente());
                XSSFCell devisevalue = row0.createCell(17);
                devisevalue.setCellValue(ii.getDevise());
                XSSFCell vmultibatch = row0.createCell(18);
                vmultibatch.setCellValue("NON");
            }
            enttvaluen.setCellValue(somme);
            workbook.write(fos);
            fos.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getLocation(String idpro) {
        List<Stocker> loc = StockerDelegate.findDescSortedByDateStock(idpro);
        if (loc.isEmpty()) {
            return null;
        }
        return loc.get(0).getLocalisation();
    }

    public static File exportXlsPhysicalInventory(HashMap<String, String> bundleData, List<ComptageItem> lisinvent,
            String dev) {
        FileOutputStream fos;
        try {
            String path = MainUI.cPath("/Media/inventories");
            File file = new File(path + "/ksf-inv_mag_" + Constants.TIMESTAMPED_FORMAT.format(new Date()) + ".xlsx");
            fos = new FileOutputStream(file);
            XSSFWorkbook workbook = new XSSFWorkbook();
            Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.DARK_BLUE.index);
            headerFont.setBold(true);

            Font headertitle = workbook.createFont();
            headertitle.setColor(IndexedColors.SKY_BLUE.index);
            headertitle.setBold(true);

            // creation de la feuil
            XSSFSheet feuil = workbook.createSheet("Inventaire magasin");
            feuil.setColumnWidth(1, 25 * 400);

            CellStyle headerCelltitle = feuil.getWorkbook().createCellStyle();
            headerCelltitle.setFont(headertitle);

            CellStyle headerCellStyle = feuil.getWorkbook().createCellStyle();
            // fill foreground color ...
            headerCellStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.index);
            // and solid fill pattern produces solid grey cell fill
            headerCellStyle.setFillPattern(FillPatternType.DIAMONDS);
            headerCellStyle.setFont(headerFont);
            int rowid = 0;
            // les entetes des colones
            ComptageItem ci = lisinvent.getFirst();
            Format df = new SimpleDateFormat("dd/MM/yyyy");
            String leo = df.format(new Date());
            XSSFRow row0 = feuil.createRow(rowid++);

            XSSFCell enttx = row0.createCell(1);
            enttx.setCellValue(bundleData.get("entrep"));

            XSSFCell enttdate = row0.createCell(10);
            enttdate.setCellValue("Fait le : " + leo);

            row0 = feuil.createRow(rowid++);
            XSSFCell enttrcm = row0.createCell(1);
            enttrcm.setCellValue(bundleData.get("rccm"));

            XSSFCell entdebut = row0.createCell(8);
            entdebut.setCellValue("Debut : " + ci.getDebutInventair().toString());

            row0 = feuil.createRow(rowid++);
            XSSFCell enttreg = row0.createCell(1);
            enttreg.setCellValue(bundleData.get("region"));

            row0 = feuil.createRow(rowid++);
            XSSFCell enttop = row0.createCell(8);
            enttop.setCellValue("Operateur : " + bundleData.get("operateur"));

            row0 = feuil.createRow(rowid++);
            XSSFCell enttdev = row0.createCell(8);
            enttdev.setCellValue("Devise : " + dev);

            row0 = feuil.createRow(rowid += 2);
            XSSFCell enttvalue = row0.createCell(8);
            enttvalue.setCellValue("Valeur : ");
            XSSFCell enttvaluen = row0.createCell(9);

            String entrep = bundleData.get("eUid");
            File f = FileUtils.pointFile(entrep + ".png");
            InputStream is;
            if (!f.exists()) {
                is = MainuiController.class.getResourceAsStream("/icons/gallery.png");
                f = FileUtils.streamTofile(is);
            }
            is = new FileInputStream(f);

            byte[] bytes = IOUtils.toByteArray(is);
            int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
            is.close();
            XSSFCreationHelper helper = workbook.getCreationHelper();
            Drawing drawing = feuil.createDrawingPatriarch();
            // add a picture shape
            XSSFClientAnchor anchor = helper.createClientAnchor();
            anchor.setCol1(0);
            anchor.setRow1(0);
            Picture pict = drawing.createPicture(anchor, pictureIdx);
            pict.resize();
            row0 = feuil.createRow(rowid += 2);
            XSSFCell enttitle = row0.createCell(5);

            enttitle.setCellValue("INVENTAIRE DE STOCK DU POINT DE VENTE ");
            enttitle.setCellStyle(headerCelltitle);
            row0 = feuil.createRow(rowid += 2);

            XSSFCell codebar = row0.createCell(0);
            codebar.setCellStyle(headerCellStyle);
            codebar.setCellValue("CODE");
            XSSFCell nom_produit = row0.createCell(1);
            nom_produit.setCellStyle(headerCellStyle);
            nom_produit.setCellValue("PRODUIT");
            XSSFCell marque = row0.createCell(2);
            marque.setCellStyle(headerCellStyle);
            marque.setCellValue("MARQUE");
            XSSFCell modele = row0.createCell(3);
            modele.setCellStyle(headerCellStyle);
            modele.setCellValue("MODELE/FORME");
            XSSFCell taille = row0.createCell(4);
            taille.setCellStyle(headerCellStyle);
            taille.setCellValue("COCENTRATION/TAILLE");
            XSSFCell lot = row0.createCell(5);
            lot.setCellStyle(headerCellStyle);
            lot.setCellValue("LOT");
            XSSFCell mesure = row0.createCell(6);
            mesure.setCellStyle(headerCellStyle);
            mesure.setCellValue("QUANTITE");
            XSSFCell entree = row0.createCell(7);
            entree.setCellStyle(headerCellStyle);
            entree.setCellValue("MESURE");
            XSSFCell pau_usd = row0.createCell(8);
            pau_usd.setCellStyle(headerCellStyle);
            pau_usd.setCellValue("P.A. UNIT");
            XSSFCell valeur_total = row0.createCell(9);
            valeur_total.setCellStyle(headerCellStyle);
            valeur_total.setCellValue("VALEUR STOCK");
            XSSFCell devise = row0.createCell(10);
            devise.setCellStyle(headerCellStyle);
            devise.setCellValue("DEVISE");
            XSSFCell local = row0.createCell(11);
            local.setCellStyle(headerCellStyle);
            local.setCellValue("LOCALISATION");
            XSSFCell date_expir = row0.createCell(12);
            date_expir.setCellStyle(headerCellStyle);
            date_expir.setCellValue("DATE EXP.");

            for (ComptageItem ii : lisinvent) {
                row0 = feuil.createRow(++rowid);
                String localisation = getLocation(ii.getProduit().getUid());
                row0.createCell(0).setCellValue(ii.getProduit().getCodebar());
                row0.createCell(1).setCellValue(ii.getProduit().getNomProduit());
                row0.createCell(2).setCellValue(ii.getProduit().getMarque());
                row0.createCell(3).setCellValue(ii.getProduit().getModele());
                row0.createCell(4)
                        .setCellValue((ii.getProduit().getTaille() == null ? "" : ii.getProduit().getTaille()));
                row0.createCell(5).setCellValue(ii.getNumlot());
                row0.createCell(6).setCellValue(ii.getQuantite());
                row0.createCell(7).setCellValue(ii.getMesure().getDescription());
                row0.createCell(8).setCellValue(ii.getCoutAchat());
                row0.createCell(9).setCellValue(ii.getCoutTotal());
                row0.createCell(10).setCellValue(dev);
                row0.createCell(11).setCellValue(localisation);
                row0.createCell(12).setCellValue(ii.getDateExpiration());
            }
            enttvaluen.setCellValue(lisinvent.stream().mapToDouble(c -> c.getCoutTotal()).sum());
            workbook.write(fos);
            fos.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File exportPDFicheStock(List<FicheItem> datas, Mesure m, Produit p) {
        try {
            String path = MainUI.cPath("/Media/fichedestocks");
            File file = new File(path + "/ksf-fiche-stock" + Constants.TIMESTAMPED_FORMAT.format(new Date()) + ".pdf");
            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(file));
            Document doc = new Document(pdfDoc, PageSize.A4.rotate());
            doc.setFontSize(20);
            doc.setBold();
            doc.setTextAlignment(TextAlignment.CENTER);
            doc.add(new Paragraph("Fiche de stock"));
            doc.add(new Paragraph("______________"));
            doc.setTextAlignment(TextAlignment.LEFT);
            doc.add(new Paragraph("Codebar : " + p.getCodebar()));
            doc.add(new Paragraph("Produit  : " + p.getNomProduit() + " " + p.getMarque() + " " + p.getModele() + " "
                    + (p.getTaille() == null ? "" : p.getTaille()) + " "
                    + (p.getCouleur() == null ? "" : p.getCouleur())));
            doc.add(new Paragraph("Mesure en " + m.getDescription() + " et devise en USD "));
            Table table = new Table(12);
            table.setFontSize(13);
            table.setWidth(UnitValue.createPercentValue(100f));
            com.itextpdf.layout.element.Cell cel1 = new com.itextpdf.layout.element.Cell(1, 1);
            cel1.add(new Paragraph("DATES"));
            table.addHeaderCell(cel1);
            com.itextpdf.layout.element.Cell cel2 = new com.itextpdf.layout.element.Cell(1, 1);
            cel2.add(new Paragraph("LIBELLES"));
            table.addHeaderCell(cel2);
            com.itextpdf.layout.element.Cell cel3q = new com.itextpdf.layout.element.Cell(1, 1);
            cel3q.add(new Paragraph("Qte. Entrees"));
            com.itextpdf.layout.element.Cell cel3pu = new com.itextpdf.layout.element.Cell(1, 1);
            cel3pu.add(new Paragraph("PU Entrees"));
            com.itextpdf.layout.element.Cell cel3pt = new com.itextpdf.layout.element.Cell(1, 1);
            cel3pt.add(new Paragraph("PT Entrees"));
            table.addHeaderCell(cel3q);
            table.addHeaderCell(cel3pu);
            table.addHeaderCell(cel3pt);
            com.itextpdf.layout.element.Cell cel4q = new com.itextpdf.layout.element.Cell(1, 1);
            cel4q.add(new Paragraph("Qte. Sorties"));
            com.itextpdf.layout.element.Cell cel4pu = new com.itextpdf.layout.element.Cell(1, 1);
            cel4pu.add(new Paragraph("CU Sorties"));
            com.itextpdf.layout.element.Cell cel4pt = new com.itextpdf.layout.element.Cell(1, 1);
            cel4pt.add(new Paragraph("CT Sorties"));
            table.addHeaderCell(cel4q);
            table.addHeaderCell(cel4pu);
            table.addHeaderCell(cel4pt);
            com.itextpdf.layout.element.Cell cel5q = new com.itextpdf.layout.element.Cell(1, 1);
            cel5q.add(new Paragraph("Qte. Stock"));
            com.itextpdf.layout.element.Cell cel5pu = new com.itextpdf.layout.element.Cell(1, 1);
            cel5pu.add(new Paragraph("CU Stock"));

            com.itextpdf.layout.element.Cell cel5pt = new com.itextpdf.layout.element.Cell(1, 1);
            cel5pt.add(new Paragraph("CT Stock"));
            table.addHeaderCell(cel5q);
            table.addHeaderCell(cel5pu);
            table.addHeaderCell(cel5pt);
            table.addHeaderCell("DEST.");
            int row = 1;
            for (FicheItem f : datas) {
                com.itextpdf.layout.element.Cell data1 = new com.itextpdf.layout.element.Cell(row, 1);
                data1.add(new Paragraph(Constants.DATE_HEURE_FORMAT.format(f.getDate())));
                table.addCell(data1);
                com.itextpdf.layout.element.Cell data2 = new com.itextpdf.layout.element.Cell(row, 1);
                data2.add(new Paragraph(f.getLibelles()));
                table.addCell(data2);
                com.itextpdf.layout.element.Cell data3 = new com.itextpdf.layout.element.Cell(row, 1);
                double qe = f.getQuantiteEntree();
                data3.add(new Paragraph(qe == 0 ? "" : String.valueOf(qe)));
                table.addCell(data3);
                com.itextpdf.layout.element.Cell data4 = new com.itextpdf.layout.element.Cell(row, 1);
                double cue = f.getPrixUnitEntree();
                data4.add(new Paragraph(cue == 0 ? "" : String.valueOf(cue)));
                table.addCell(data4);
                com.itextpdf.layout.element.Cell data5 = new com.itextpdf.layout.element.Cell(row, 1);
                double cte = f.getCoutTotalEntree();
                data5.add(new Paragraph(cte == 0 ? "" : String.valueOf(cte)));
                table.addCell(data5);
                com.itextpdf.layout.element.Cell data6 = new com.itextpdf.layout.element.Cell(row, 1);
                double qs = f.getQuantiteSortie();
                data6.add(new Paragraph(qs == 0 ? "" : String.valueOf(qs)));
                table.addCell(data6);
                com.itextpdf.layout.element.Cell data7 = new com.itextpdf.layout.element.Cell(row, 1);
                double cus = f.getCoutUnitaireSortie();
                data7.add(new Paragraph(cus == 0 ? "" : String.valueOf(cus)));
                table.addCell(data7);
                com.itextpdf.layout.element.Cell data8 = new com.itextpdf.layout.element.Cell(row, 1);
                double cts = f.getCoutTotalSortie();
                data8.add(new Paragraph(cts == 0 ? "" : String.valueOf(cts)));
                table.addCell(data8);
                com.itextpdf.layout.element.Cell data9 = new com.itextpdf.layout.element.Cell(row, 1);
                data9.add(new Paragraph(String.valueOf(f.getQuantiteRestant())));
                table.addCell(data9);
                com.itextpdf.layout.element.Cell data10 = new com.itextpdf.layout.element.Cell(row, 1);
                data10.add(new Paragraph(String.valueOf(f.getCoutUnitRestant())));
                table.addCell(data10);
                com.itextpdf.layout.element.Cell data11 = new com.itextpdf.layout.element.Cell(row, 1);
                data11.add(new Paragraph(String.valueOf(f.getCoutTotalRestant())));
                table.addCell(data11);
                com.itextpdf.layout.element.Cell data12 = new com.itextpdf.layout.element.Cell(row, 1);
                data12.add(new Paragraph(f.getDestination() == null ? "" : f.getDestination()));
                table.addCell(data12);
                row++;
                System.out.println("E = " + f.getQuantiteEntree() + " S = " + f.getQuantiteSortie() + " a "
                        + f.getCoutUnitaireSortie() + " R =" + f.getQuantiteRestant());
            }
            doc.add(table);
            doc.close();
            return file;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static File exportPDFicheDebiteurs(List<DebtItem> datas) {
        try {
            String path = MainUI.cPath("/Media/autres");
            File file = new File(
                    path + "/ksf-fiche-recouvrement_" + Constants.TIMESTAMPED_FORMAT.format(new Date()) + ".pdf");
            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(file));
            Document doc = new Document(pdfDoc, PageSize.A4.rotate());
            doc.setFontSize(18);
            doc.setBold();
            doc.setTextAlignment(TextAlignment.CENTER);
            doc.add(new Paragraph("FICHE DE RECOUVREMENT DES DETTES DE CLIENTS"));
            doc.add(new Paragraph("______________________________________________"));
            doc.setTextAlignment(TextAlignment.LEFT);
            doc.add(new Paragraph());
            doc.add(new Paragraph("Les montants sont en USD "));
            Table table = new Table(8);
            table.setFontSize(12);
            table.setWidth(UnitValue.createPercentValue(95f));
            com.itextpdf.layout.element.Cell cel1 = new com.itextpdf.layout.element.Cell(1, 1);
            cel1.add(new Paragraph("DATES"));
            table.addHeaderCell(cel1);
            com.itextpdf.layout.element.Cell cel2 = new com.itextpdf.layout.element.Cell(1, 1);
            cel2.add(new Paragraph("NOMS"));
            table.addHeaderCell(cel2);
            com.itextpdf.layout.element.Cell cel3q = new com.itextpdf.layout.element.Cell(1, 1);
            cel3q.add(new Paragraph("TELEPHONES"));
            com.itextpdf.layout.element.Cell cel3pu = new com.itextpdf.layout.element.Cell(1, 1);
            cel3pu.add(new Paragraph("FACTURES"));
            com.itextpdf.layout.element.Cell cel3pt = new com.itextpdf.layout.element.Cell(1, 1);
            cel3pt.add(new Paragraph("MONTANT TOTAL"));
            table.addHeaderCell(cel3q);
            table.addHeaderCell(cel3pu);
            table.addHeaderCell(cel3pt);
            com.itextpdf.layout.element.Cell cel4q = new com.itextpdf.layout.element.Cell(1, 1);
            cel4q.add(new Paragraph("MONTANT PAYES"));
            table.addHeaderCell(cel4q);
            com.itextpdf.layout.element.Cell cel4pu = new com.itextpdf.layout.element.Cell(1, 1);
            cel4pu.add(new Paragraph("MONTANT RESTANT"));
            table.addHeaderCell(cel4pu);
            table.addHeaderCell("OBSERVATION");
            int row = 1;
            for (DebtItem item : datas) {
                com.itextpdf.layout.element.Cell data1 = new com.itextpdf.layout.element.Cell(row, 1);
                data1.add(new Paragraph(Constants.DATE_HEURE_FORMAT.format(item.getDate())));
                table.addCell(data1);
                com.itextpdf.layout.element.Cell data2 = new com.itextpdf.layout.element.Cell(row, 1);
                data2.add(new Paragraph(item.getNomClient()));
                table.addCell(data2);
                com.itextpdf.layout.element.Cell data3 = new com.itextpdf.layout.element.Cell(row, 1);
                data3.add(new Paragraph(item.getPhoneClient()));
                table.addCell(data3);
                com.itextpdf.layout.element.Cell data4 = new com.itextpdf.layout.element.Cell(row, 1);
                data4.add(new Paragraph(item.getFacture()));
                table.addCell(data4);
                com.itextpdf.layout.element.Cell data5 = new com.itextpdf.layout.element.Cell(row, 1);
                double cte = item.getMontantDette();
                data5.add(new Paragraph(cte == 0 ? "" : String.valueOf(cte)));
                table.addCell(data5);
                com.itextpdf.layout.element.Cell data6 = new com.itextpdf.layout.element.Cell(row, 1);
                double qs = item.getMontantPaye();
                data6.add(new Paragraph(qs == 0 ? "" : String.valueOf(qs)));
                table.addCell(data6);
                com.itextpdf.layout.element.Cell data7 = new com.itextpdf.layout.element.Cell(row, 1);
                double cus = item.getMontantRestant();
                data7.add(new Paragraph(cus == 0 ? "" : String.valueOf(cus)));
                table.addCell(data7);
                com.itextpdf.layout.element.Cell data8 = new com.itextpdf.layout.element.Cell(row, 1);
                data8.add(new Paragraph("       "));
                table.addCell(data8);
                row++;
            }
            doc.add(table);
            doc.close();
            return file;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static File exportPDFRecouvrementClient(String numeroRecu, LocalDateTime datePaiement, String client,
            String referenceDette,
            String compte, double montantUsd, double montantCdf, double soldeRestantUsd, String observation) {
        try {
            String path = MainUI.cPath("/Media/autres");
            File file = new File(
                    path + "/ksf-recu-recouvrement_" + Constants.TIMESTAMPED_FORMAT.format(new Date()) + ".pdf");
            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(file));
            Document doc = new Document(pdfDoc, PageSize.A4);
            doc.setFontSize(16);
            doc.setBold();
            doc.setTextAlignment(TextAlignment.CENTER);
            doc.add(new Paragraph("RECU DE REGLEMENT CLIENT"));
            doc.add(new Paragraph("________________________________"));
            doc.setTextAlignment(TextAlignment.LEFT);
            doc.setFontSize(11);
            doc.setBold();
            doc.add(new Paragraph("Numero recu : " + (numeroRecu == null ? "-" : numeroRecu)));
            doc.add(new Paragraph("Date : " + (datePaiement == null ? "-" : datePaiement.toString())));
            doc.add(new Paragraph("Client : " + (client == null ? "-" : client)));
            doc.add(new Paragraph("Reference dette : " + (referenceDette == null ? "-" : referenceDette)));
            doc.add(new Paragraph("Compte de reception : " + (compte == null ? "-" : compte)));
            doc.add(new Paragraph(
                    "Montant recu (USD) : " + BigDecimal.valueOf(montantUsd).setScale(2, RoundingMode.HALF_EVEN)));
            doc.add(new Paragraph(
                    "Montant recu (CDF) : " + BigDecimal.valueOf(montantCdf).setScale(2, RoundingMode.HALF_EVEN)));
            doc.add(new Paragraph("Solde restant (USD) : "
                    + BigDecimal.valueOf(soldeRestantUsd).setScale(2, RoundingMode.HALF_EVEN)));
            doc.add(new Paragraph(
                    "Observation : " + (observation == null || observation.isBlank() ? "-" : observation)));
            doc.add(new Paragraph(""));
            doc.add(new Paragraph("Signature caisse : ___________________________"));
            doc.add(new Paragraph("Signature client : ___________________________"));
            doc.close();
            return file;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static List<FicheItem> merge(List<Mesure> lmzr, List<Stocker> ls, List<Destocker> ld, Produit p) {
        List<FicheItem> result = new ArrayList<>();
        List<Stocker> calc = new ArrayList<>();

        for (Stocker s : ls) {
            calc.add(s);
            Mesure m = findMesure(lmzr, s.getMesureId());
            double q = s.getQuantite() * m.getQuantContenu();
            FicheItem fi = new FicheItem();
            fi.setLibelles(s.getLibelle());
            fi.setDate(Constants.Datetime.toUtilDate(s.getDateStocker().toLocalDate()));
            fi.setMesure(m);
            fi.setPrixUnitEntree(s.getCoutAchat() / m.getQuantContenu());
            fi.setQuantiteEntree(q);
            fi.setCoutTotalEntree(((s.getCoutAchat() / m.getQuantContenu()) * q));
            fi.setUidRef(s.getUid());
            fi.setUidProduit(p.getUid());
            // cump = getCump(lmzr, m, calc, p);
            // fi.setCoutUnitRestant(cump);
            result.add(fi);
        }

        for (Destocker d : ld) {
            Mesure m = findMesure(lmzr, d.getMesureId());
            double q = d.getQuantite() * m.getQuantContenu();
            FicheItem fi = new FicheItem();
            fi.setCoutUnitaireSortie(d.getCoutAchat() / m.getQuantContenu());
            fi.setDate(Constants.Datetime.toUtilDate(d.getDateDestockage().toLocalDate()));
            fi.setMesure(m);
            fi.setLibelles(d.getLibelle());
            fi.setQuantiteSortie(q);
            fi.setCoutTotalSortie((q * (d.getCoutAchat() / m.getQuantContenu())));
            fi.setUidRef(d.getUid());
            fi.setDestination(d.getDestination());
            fi.setUidProduit(p.getUid());
            result.add(fi);
        }
        return result;
    }

    private static List<FicheItem> sort(List<FicheItem> fich) {
        Collections.sort(fich, new FicheItem());
        return fich;
    }

    private static double sumE(List<FicheItem> fi) {
        double r = 0;
        for (FicheItem f : fi) {
            r += f.getQuantiteEntree();
        }
        return r;
    }

    private static double sumS(List<FicheItem> fi) {
        double r = 0;
        for (FicheItem f : fi) {
            r += f.getQuantiteSortie();
        }
        return r;
    }

    public static List<FicheItem> findFicheDeStock(Mesure m, List<Mesure> lmzr, List<Stocker> ls, List<Destocker> ld,
            Produit p) {
        List<FicheItem> result = new ArrayList<>();
        List<FicheItem> fii = calculerFicheDeStock(lmzr, ls, ld, p);

        for (FicheItem fi : fii) {
            double cump = fi.getCoutUnitaireSortie() * (m == null ? 1 : m.getQuantContenu());
            double pue = fi.getPrixUnitEntree() * (m == null ? 1 : m.getQuantContenu());
            double qe = fi.getQuantiteEntree() / (m == null ? 1 : m.getQuantContenu());
            double qs = fi.getQuantiteSortie() / (m == null ? 1 : m.getQuantContenu());
            double qr = fi.getQuantiteRestant() / (m == null ? 1 : m.getQuantContenu());
            double cumpr = fi.getCoutUnitRestant() * (m == null ? 1 : m.getQuantContenu());
            double ctr = cumpr * qr;
            fi.setCoutUnitaireSortie(BigDecimal.valueOf(cump).setScale(3, RoundingMode.HALF_EVEN).doubleValue());
            fi.setPrixUnitEntree(BigDecimal.valueOf(pue).setScale(3, RoundingMode.HALF_EVEN).doubleValue());
            fi.setQuantiteEntree(BigDecimal.valueOf(qe).setScale(3, RoundingMode.HALF_EVEN).doubleValue());
            fi.setQuantiteSortie(BigDecimal.valueOf(qs).setScale(3, RoundingMode.HALF_EVEN).doubleValue());
            fi.setQuantiteRestant(BigDecimal.valueOf(qr).setScale(3, RoundingMode.HALF_EVEN).doubleValue());
            fi.setCoutUnitRestant(BigDecimal.valueOf(cumpr).setScale(3, RoundingMode.HALF_EVEN).doubleValue());
            fi.setCoutTotalRestant(BigDecimal.valueOf(ctr).setScale(3, RoundingMode.HALF_EVEN).doubleValue());
            result.add(fi);
        }
        return result;
    }

    public static List<FicheItem> findFicheDeStock(Mesure m, List<Mesure> lmzr, List<Stocker> ls, List<Destocker> ld,
            Produit p, long debut, long fin) {
        List<FicheItem> result = new ArrayList<>();
        System.err.println(" debut " + debut + " fin " + fin);
        List<FicheItem> fii = calculerFicheDeStock(lmzr, ls, ld, p);
        for (FicheItem fi : fii) {
            double cump = fi.getCoutUnitaireSortie() * (m == null ? 1 : m.getQuantContenu());
            double pue = fi.getPrixUnitEntree() * (m == null ? 1 : m.getQuantContenu());
            double qe = fi.getQuantiteEntree() / (m == null ? 1 : m.getQuantContenu());
            double qs = fi.getQuantiteSortie() / (m == null ? 1 : m.getQuantContenu());
            double qr = fi.getQuantiteRestant() / (m == null ? 1 : m.getQuantContenu());
            double cumpr = fi.getCoutUnitRestant() * (m == null ? 1 : m.getQuantContenu());
            double ctr = cumpr * qr;
            fi.setCoutUnitaireSortie(BigDecimal.valueOf(cump).setScale(3, RoundingMode.HALF_EVEN).doubleValue());
            fi.setPrixUnitEntree(BigDecimal.valueOf(pue).setScale(3, RoundingMode.HALF_EVEN).doubleValue());
            fi.setQuantiteEntree(BigDecimal.valueOf(qe).setScale(3, RoundingMode.HALF_EVEN).doubleValue());
            fi.setQuantiteSortie(BigDecimal.valueOf(qs).setScale(3, RoundingMode.HALF_EVEN).doubleValue());
            fi.setQuantiteRestant(BigDecimal.valueOf(qr).setScale(3, RoundingMode.HALF_EVEN).doubleValue());
            fi.setCoutUnitRestant(BigDecimal.valueOf(cumpr).setScale(3, RoundingMode.HALF_EVEN).doubleValue());
            fi.setCoutTotalRestant(BigDecimal.valueOf(ctr).setScale(3, RoundingMode.HALF_EVEN).doubleValue());
            long date = fi.getDate().getTime();
            if (date >= debut && date <= fin) {
                result.add(fi);
            }
        }
        return result;
    }

    private static List<FicheItem> calculerFicheDeStock(List<Mesure> lmzr, List<Stocker> ls, List<Destocker> ld,
            Produit p) {
        List<FicheItem> fis = merge(lmzr, ls, ld, p);
        List<FicheItem> result = new ArrayList<>();
        fis = sort(fis);
        List<FicheItem> fix = new ArrayList<>();
        double e = 0, s = 0, qr = 0, cumpr = 0, ctr = 0, draft = 0;
        for (int i = 0; i < fis.size(); i++) {
            FicheItem fi = fis.get(i);
            fix.add(fi);
            e = sumE(fix);
            s = sumS(fix);
            if (qr == 0) {
                fi.setLibelles("Stock Initial");
                qr = (e - s);
            } else {
                if (fi.getQuantiteEntree() > 0) {
                    qr += fi.getQuantiteEntree();
                }
                if (fi.getQuantiteSortie() > 0) {
                    qr -= fi.getQuantiteSortie();
                }
            }

            if (cumpr == 0) {
                cumpr = fi.getPrixUnitEntree();
            } else {
                if (fi.getQuantiteEntree() > 0) {
                    draft = (ctr + fi.getCoutTotalEntree());
                    cumpr = draft / qr;
                }
            }
            ctr = cumpr * qr;
            fi.setQuantiteRestant(qr);
            fi.setCoutUnitRestant(cumpr);
            fi.setCoutTotalRestant(ctr);
            result.add(fi);
        }
        return result;
    }

    public static List<LigneVente> findLigneVenteForVente(List<LigneVente> findAll, Integer uid) {
        List<LigneVente> rst = new ArrayList<>();
        for (LigneVente l : findAll) {
            Vente v = l.getReference();
            if (v == null) {
                continue;
            }
            if (v.getUid() == uid) {
                rst.add(l);
            }
        }
        return rst;
    }

    public static File exportRelevee(Collection<Relevee> relevees, String entrep, double total, String type) {
        String path = MainUI.cPath("/Media/autres");
        File file = new File(path + "/ksf-Relevee_" + Constants.TIMESTAMPED_FORMAT.format(new Date()) + ".xls");
        if (type.equalsIgnoreCase("xls")) {
            FileOutputStream fos;
            try {

                fos = new FileOutputStream(file);
                HSSFWorkbook hsswb = new HSSFWorkbook();
                // creation de la feuil
                HSSFSheet feuil = hsswb.createSheet("Relevee");
                int rowid = 0;
                double som = 0;
                // les entetes des colones
                HSSFRow row0 = feuil.createRow(rowid);
                Cell codebar = row0.createCell(0);
                codebar.setCellValue("DATES");
                Cell nom_produit = row0.createCell(1);
                nom_produit.setCellValue("BONS");
                Cell quantite_entree = row0.createCell(2);
                quantite_entree.setCellValue("DEPEDANTS");
                Cell quantite_sortie = row0.createCell(3);
                quantite_sortie.setCellValue("PRODUITS");
                Cell stock_alerte = row0.createCell(4);
                stock_alerte.setCellValue("QUANTITES");
                Cell unite = row0.createCell(5);
                unite.setCellValue("UNITE");
                Cell date_expir = row0.createCell(6);
                date_expir.setCellValue("PRIX UNIT");
                Cell soldeUsd = row0.createCell(7);
                soldeUsd.setCellValue("TOTAUX");
                Cell soldeCdf = row0.createCell(8);
                soldeCdf.setCellValue("AGENTS");
                Cell observ = row0.createCell(9);
                observ.setCellValue("OBS.");
                for (Relevee ii : relevees) {
                    row0 = feuil.createRow(++rowid);
                    Cell codebar1 = row0.createCell(0);
                    codebar1.setCellValue(Constants.USER_READABLE_FORMAT.format(ii.getDate()));
                    Cell ref = row0.createCell(1);
                    ref.setCellValue(ii.getNumeroBon());
                    Cell nom_prod = row0.createCell(2);
                    nom_prod.setCellValue(ii.getNomClient());
                    Cell quant_in = row0.createCell(3);
                    quant_in.setCellValue(ii.getNomProduit());
                    Cell quant_out = row0.createCell(4);
                    quant_out.setCellValue(ii.getQuantite());
                    Cell unite1 = row0.createCell(5);
                    unite1.setCellValue(ii.getMesure().getDescription());
                    Cell dexpir = row0.createCell(6);
                    dexpir.setCellValue(ii.getPrixunitaire());
                    Cell susd = row0.createCell(7);
                    double s = ii.getPrixunitaire() * ii.getQuantite();
                    susd.setCellValue(s);
                    Cell scdf = row0.createCell(8);
                    scdf.setCellValue(ii.getParent());
                    som += s;
                }
                row0 = feuil.createRow(++rowid);
                Cell tot = row0.createCell(0);
                tot.setCellValue("TOTAL");
                Cell ref = row0.createCell(4);
                ref.setCellValue(som);
                hsswb.write(fos);
                fos.close();
                return file;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (type.equalsIgnoreCase("pdf")) {
            try {
                PdfDocument pdfDoc = new PdfDocument(new PdfWriter(file));
                Document doc = new Document(pdfDoc, PageSize.A4.rotate());
                doc.setFontSize(20);
                doc.setBold();
                doc.setTextAlignment(TextAlignment.CENTER);

                doc.add(new Paragraph("RELEVEE DE CONSOMMATION POUR " + entrep));
                doc.setTextAlignment(TextAlignment.LEFT);

                doc.add(new Paragraph("Date : " + Constants.DATE_HEURE_FORMAT.format(new Date())));
                doc.add(new Paragraph("Code : " + ((int) (Math.random() * 100000))));
                doc.add(new Paragraph("Total : " + total + " USD"));
                Table table = new Table(8);
                table.setFontSize(13);
                table.setWidth(UnitValue.createPercentValue(100f));
                com.itextpdf.layout.element.Cell cel1 = new com.itextpdf.layout.element.Cell(1, 1);
                cel1.add(new Paragraph("DATES"));
                table.addHeaderCell(cel1);
                com.itextpdf.layout.element.Cell cel2 = new com.itextpdf.layout.element.Cell(1, 1);
                cel2.add(new Paragraph("BONS"));
                table.addHeaderCell(cel2);
                com.itextpdf.layout.element.Cell cel3 = new com.itextpdf.layout.element.Cell(1, 1);
                cel3.add(new Paragraph("DEPENDANT"));
                table.addHeaderCell(cel3);
                com.itextpdf.layout.element.Cell cel4 = new com.itextpdf.layout.element.Cell(1, 1);
                cel4.add(new Paragraph("PRODUITS"));
                table.addHeaderCell(cel4);
                com.itextpdf.layout.element.Cell cel5 = new com.itextpdf.layout.element.Cell(1, 1);
                cel5.add(new Paragraph("QUANTITES"));
                table.addHeaderCell(cel5);
                com.itextpdf.layout.element.Cell cel6 = new com.itextpdf.layout.element.Cell(1, 1);
                cel6.add(new Paragraph("PRIX UNIT."));
                table.addHeaderCell(cel6);
                com.itextpdf.layout.element.Cell cel7 = new com.itextpdf.layout.element.Cell(1, 1);
                cel7.add(new Paragraph("TOTAUX"));
                table.addHeaderCell(cel7);
                com.itextpdf.layout.element.Cell cel8 = new com.itextpdf.layout.element.Cell(1, 1);
                cel8.add(new Paragraph("AGENTS"));
                table.addHeaderCell(cel8);
                int row = 1;
                for (Relevee f : relevees) {
                    com.itextpdf.layout.element.Cell data1 = new com.itextpdf.layout.element.Cell(row, 1);
                    data1.add(new Paragraph(Constants.DATE_HEURE_FORMAT.format(f.getDate())));
                    table.addCell(data1);
                    com.itextpdf.layout.element.Cell data2 = new com.itextpdf.layout.element.Cell(row, 1);
                    data2.add(new Paragraph(f.getNumeroBon()));
                    table.addCell(data2);
                    com.itextpdf.layout.element.Cell data3 = new com.itextpdf.layout.element.Cell(row, 1);
                    data3.add(new Paragraph(f.getNomClient()));
                    table.addCell(data3);
                    com.itextpdf.layout.element.Cell data4 = new com.itextpdf.layout.element.Cell(row, 1);
                    data4.add(new Paragraph(f.getNomProduit()));
                    table.addCell(data4);
                    com.itextpdf.layout.element.Cell data5 = new com.itextpdf.layout.element.Cell(row, 1);
                    data5.add(new Paragraph(String.valueOf(f.getQuantite())));
                    table.addCell(data5);
                    com.itextpdf.layout.element.Cell data6 = new com.itextpdf.layout.element.Cell(row, 1);
                    data6.add(new Paragraph(String.valueOf(f.getPrixunitaire())));
                    table.addCell(data6);
                    com.itextpdf.layout.element.Cell data7 = new com.itextpdf.layout.element.Cell(row, 1);
                    data7.add(new Paragraph(String.valueOf((f.getQuantite() * f.getPrixunitaire()))));
                    table.addCell(data7);
                    com.itextpdf.layout.element.Cell data8 = new com.itextpdf.layout.element.Cell(row, 1);
                    data8.add(new Paragraph(f.getParent()));
                    table.addCell(data8);
                    row++;
                }
                doc.add(table);
                doc.close();
                return file;
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public static File exportPerimees(HashMap<String, String> bundleData, ObservableList<Peremption> items, String dev,
            LocalDate d1, LocalDate d2, int dec_flag) {
        FileOutputStream fos;
        try {
            String path = MainUI.cPath("/Media/inventories");
            File file = new File(
                    path + "/ksf-inv_expiree_" + Constants.TIMESTAMPED_FORMAT.format(new Date()) + ".xlsx");
            fos = new FileOutputStream(file);
            XSSFWorkbook workbook = new XSSFWorkbook();
            Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.DARK_BLUE.index);
            headerFont.setBold(true);

            Font headertitle = workbook.createFont();
            headertitle.setColor(IndexedColors.SKY_BLUE.index);
            headertitle.setBold(true);

            // creation de la feuil
            XSSFSheet feuil = workbook.createSheet("Inventaire des expirees ");
            feuil.setColumnWidth(1, 25 * 400);

            CellStyle headerCelltitle = feuil.getWorkbook().createCellStyle();
            headerCelltitle.setFont(headertitle);

            CellStyle headerCellStyle = feuil.getWorkbook().createCellStyle();
            // fill foreground color ...
            headerCellStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.index);
            // and solid fill pattern produces solid grey cell fill
            headerCellStyle.setFillPattern(FillPatternType.DIAMONDS);
            headerCellStyle.setFont(headerFont);
            int rowid = 0;
            // les entetes des colones

            Format df = new SimpleDateFormat("dd/MM/yyyy");
            String leo = df.format(new Date());
            XSSFRow row0 = feuil.createRow(rowid++);

            XSSFCell enttx = row0.createCell(1);
            enttx.setCellValue(bundleData.get("entrep"));

            XSSFCell enttdate = row0.createCell(10);
            enttdate.setCellValue("Fait le : " + leo);

            row0 = feuil.createRow(rowid++);
            XSSFCell enttrcm = row0.createCell(1);
            enttrcm.setCellValue(bundleData.get("rccm"));

            XSSFCell entdebut = row0.createCell(8);
            entdebut.setCellValue("Date : " + LocalDate.now());

            row0 = feuil.createRow(rowid++);
            XSSFCell enttreg = row0.createCell(1);
            enttreg.setCellValue(bundleData.get("region"));

            row0 = feuil.createRow(rowid++);
            XSSFCell enttop = row0.createCell(8);
            enttop.setCellValue("Operateur : " + bundleData.get("operateur"));

            row0 = feuil.createRow(rowid++);
            XSSFCell enttdev = row0.createCell(8);
            enttdev.setCellValue("Devise : " + dev);

            row0 = feuil.createRow(rowid += 2);
            XSSFCell enttvalue = row0.createCell(8);
            enttvalue.setCellValue("Valeur : ");
            XSSFCell enttvaluen = row0.createCell(9);

            String entrep = bundleData.get("eUid");
            File f = FileUtils.pointFile(entrep + ".png");
            InputStream is;
            if (!f.exists()) {
                is = MainuiController.class.getResourceAsStream("/icons/gallery.png");
                f = FileUtils.streamTofile(is);
            }
            is = new FileInputStream(f);

            byte[] bytes = IOUtils.toByteArray(is);
            int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
            is.close();
            XSSFCreationHelper helper = workbook.getCreationHelper();
            Drawing drawing = feuil.createDrawingPatriarch();
            // add a picture shape
            XSSFClientAnchor anchor = helper.createClientAnchor();
            anchor.setCol1(0);
            anchor.setRow1(0);
            Picture pict = drawing.createPicture(anchor, pictureIdx);
            pict.resize();
            row0 = feuil.createRow(rowid += 2);
            XSSFCell enttitle = row0.createCell(2);

            enttitle.setCellValue(
                    "INVENTAIRE DE STOCK DES EXPIREES EN PERIODE DE " + d1.toString() + " AU " + d2.toString());
            enttitle.setCellStyle(headerCelltitle);
            row0 = feuil.createRow(rowid += 2);

            XSSFCell codebar = row0.createCell(0);
            codebar.setCellStyle(headerCellStyle);
            codebar.setCellValue("CODE");
            XSSFCell nom_produit = row0.createCell(1);
            nom_produit.setCellStyle(headerCellStyle);
            nom_produit.setCellValue("PRODUIT");
            XSSFCell marque = row0.createCell(2);
            marque.setCellStyle(headerCellStyle);
            marque.setCellValue("MARQUE");
            XSSFCell modele = row0.createCell(3);
            modele.setCellStyle(headerCellStyle);
            modele.setCellValue("MODELE/FORME");
            XSSFCell taille = row0.createCell(4);
            taille.setCellStyle(headerCellStyle);
            taille.setCellValue("COCENTRATION/TAILLE");
            XSSFCell lot = row0.createCell(5);
            lot.setCellStyle(headerCellStyle);
            lot.setCellValue("LOT");
            XSSFCell mesure = row0.createCell(6);
            mesure.setCellStyle(headerCellStyle);
            mesure.setCellValue("QUANTITE");
            XSSFCell entree = row0.createCell(7);
            entree.setCellStyle(headerCellStyle);
            entree.setCellValue("MESURE");
            XSSFCell pau_usd = row0.createCell(8);
            pau_usd.setCellStyle(headerCellStyle);
            pau_usd.setCellValue("P.A. UNIT");
            XSSFCell valeur_total = row0.createCell(9);
            valeur_total.setCellStyle(headerCellStyle);
            valeur_total.setCellValue("VALEUR STOCK");
            XSSFCell devise = row0.createCell(10);
            devise.setCellStyle(headerCellStyle);
            devise.setCellValue("DEVISE");
            XSSFCell local = row0.createCell(11);
            local.setCellStyle(headerCellStyle);
            local.setCellValue("LOCALISATION");
            XSSFCell date_expir = row0.createCell(12);
            date_expir.setCellStyle(headerCellStyle);
            date_expir.setCellValue("DATE EXP.");
            XSSFCell decflag = row0.createCell(13);
            decflag.setCellStyle(headerCellStyle);
            decflag.setCellValue("DECLASSE");

            for (Peremption ii : items) {
                row0 = feuil.createRow(++rowid);
                Produit p = ProduitDelegate.findProduit(ii.getProduitUid());
                String localisation = getLocation(ii.getProduitUid());
                row0.createCell(0).setCellValue(p.getCodebar());
                row0.createCell(1).setCellValue(p.getNomProduit());
                row0.createCell(2).setCellValue(p.getMarque());
                row0.createCell(3).setCellValue(p.getModele());
                row0.createCell(4).setCellValue((p.getTaille() == null ? "" : p.getTaille()));
                row0.createCell(5).setCellValue(ii.getLot());
                row0.createCell(6).setCellValue(ii.getQuantite());
                row0.createCell(7).setCellValue(ii.getMesure());
                row0.createCell(8).setCellValue(ii.getCoutAchat());
                row0.createCell(9).setCellValue(ii.getValeur());
                row0.createCell(10).setCellValue(dev);
                row0.createCell(11).setCellValue(localisation);
                row0.createCell(12).setCellValue(ii.getDateExpiry());
                row0.createCell(13).setCellValue(dec_flag == 1 ? "OUI" : "NON");
            }
            enttvaluen.setCellValue(items.stream().mapToDouble(c -> c.getValeur()).sum());
            workbook.write(fos);
            fos.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static HashMap<String, Boolean> checkAccess() {
        HashMap<String, Boolean> toActivate = new HashMap<>();
        HashMap<String, List<PermitTo>> hashPerms = new HashMap<>();
        hashPerms.put(Constants.AGENTS, List.of(PermitTo.CREATE_ENGAGEMENT,
                PermitTo.UPDATE_ENGAGEMENT, PermitTo.DELETE_ENGAGEMENT));
        b1: for (Map.Entry<String, List<PermitTo>> entry : hashPerms.entrySet()) {
            for (PermitTo permitTo : entry.getValue()) {
                if (PermissionDelegate.hasPermission(permitTo)) {
                    toActivate.put(entry.getKey(), Boolean.TRUE);
                    continue b1;
                }
            }
        }
        return toActivate;
    }

    public static void exportXlsAmortissement(List<Immobilisation> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        try (Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Tableau d'amortissement");

            String[] columns = { "UID", "Libellé", "Catégorie", "Date Acquisition", "Valeur Origine (USD)",
                    "Valeur Résiduelle (USD)", "Durée (Mois)", "Dotation Mensuelle (USD)", "Amortissement Cumulé (USD)",
                    "Valeur Nette (USD)" };
            Row headerRow = sheet.createRow(0);

            org.apache.poi.ss.usermodel.CellStyle headerCellStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerCellStyle.setFont(headerFont);

            for (int i = 0; i < columns.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowNum = 1;
            java.time.LocalDate now = java.time.LocalDate.now();
            for (Immobilisation imm : list) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(imm.getUid());
                row.createCell(1).setCellValue(imm.getLibelle());
                row.createCell(2).setCellValue(imm.getCategorie());
                row.createCell(3)
                        .setCellValue(imm.getDateAcquisition() != null ? imm.getDateAcquisition().toString() : "");
                row.createCell(4).setCellValue(imm.getValeurOrigineUsd());
                row.createCell(5).setCellValue(imm.getValeurResiduelleUsd());
                row.createCell(6).setCellValue(imm.getDureeAmortissementMois());
                row.createCell(7).setCellValue(imm.dotationMensuelleUsd());
                row.createCell(8).setCellValue(imm.amortissementCumulUsd(now));
                row.createCell(9).setCellValue(imm.valeurNetteUsd(now));
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Enregistrer le tableau d'amortissement");
            fileChooser.setInitialFileName("Tableau_Amortissement_" + System.currentTimeMillis() + ".xlsx");
            fileChooser.getExtensionFilters()
                    .add(new javafx.stage.FileChooser.ExtensionFilter("Fichier Excel", "*.xlsx"));
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void exportXlsSuppliersDebt(List<Fournisseur> list) {
        try (Workbook workbook = new XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Dettes Fournisseurs");
            String[] columns = { "UID", "Nom Fournisseur", "Adresse", "Téléphone", "Dette Totale (USD)" };

            Row headerRow = sheet.createRow(0);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerCellStyle.setFont(headerFont);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowNum = 1;
            for (Fournisseur f : list) {
                double totalDebt = f.getLivraisonList() == null ? 0d
                        : f.getLivraisonList().stream().mapToDouble(l -> l.getRemained()).sum();
                if (totalDebt > 0) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(f.getUid());
                    row.createCell(1).setCellValue(f.getNomFourn());
                    row.createCell(2).setCellValue(f.getAdresse());
                    row.createCell(3).setCellValue(f.getPhone());
                    row.createCell(4).setCellValue(totalDebt);
                }
            }

            String path = System.getProperty("user.home") + "/Documents/Kazisafex/Reports";
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(path + "/Dettes_Fournisseurs_Global_" + System.currentTimeMillis() + ".xlsx");
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
            Desktop.getDesktop().open(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void exportXlsSupplierStatement(Fournisseur f, List<Livraison> debts) {
        try (Workbook workbook = new XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Relevé de Dette - " + f.getNomFourn());

            // Header Info
            Row r0 = sheet.createRow(0);
            r0.createCell(0).setCellValue("Fournisseur:");
            r0.createCell(1).setCellValue(f.getNomFourn());

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Téléphone:");
            r1.createCell(1).setCellValue(f.getPhone());

            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("Date du Relevé:");
            r2.createCell(1).setCellValue(LocalDate.now().toString());

            String[] columns = { "Date", "Num Pièce", "Libellé", "Montant Facturé (USD)", "Montant Payé (USD)",
                    "Reste à Payer (USD)" };

            Row headerRow = sheet.createRow(4);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerCellStyle.setFont(headerFont);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowNum = 5;
            double totalDebt = 0;
            for (Livraison l : debts) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(l.getDateLivr() != null ? l.getDateLivr().toString() : "");
                row.createCell(1).setCellValue(l.getNumPiece());
                row.createCell(2).setCellValue(l.getLibelle());
                row.createCell(3).setCellValue(l.getTopay() != null ? l.getTopay() : 0d);
                row.createCell(4).setCellValue(l.getPayed() != null ? l.getPayed() : 0d);
                row.createCell(5).setCellValue(l.getRemained() != null ? l.getRemained() : 0d);
                totalDebt += (l.getRemained() != null ? l.getRemained() : 0d);
            }

            Row footRow = sheet.createRow(rowNum + 1);
            Cell footCellLabel = footRow.createCell(4);
            footCellLabel.setCellValue("TOTAL DETTE:");
            CellStyle footStyle = workbook.createCellStyle();
            Font footFont = workbook.createFont();
            footFont.setBold(true);
            footStyle.setFont(footFont);
            footCellLabel.setCellStyle(footStyle);

            Cell footCellVal = footRow.createCell(5);
            footCellVal.setCellValue(totalDebt);
            footCellVal.setCellStyle(footStyle);

            String path = System.getProperty("user.home") + "/Documents/Kazisafex/Reports";
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(path + "/Releve_Fournisseur_" + f.getNomFourn().replaceAll(" ", "_") + "_" + System.currentTimeMillis() + ".xlsx");
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
            Desktop.getDesktop().open(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void exportXlsExpiredStock(List<utilities.Peremption> list) {
        try (Workbook workbook = new XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Produits Expirés");
            String[] columns = { "Codebar", "Produit", "Lot", "Localisation", "Région", "Mesure", "Quantité",
                    "Cout Achat", "Valeur Total", "Date Expiration" };

            Row headerRow = sheet.createRow(0);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerCellStyle.setFont(headerFont);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowNum = 1;
            for (utilities.Peremption p : list) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(p.getCodebar());
                row.createCell(1).setCellValue(p.getProduit());
                row.createCell(2).setCellValue(p.getLot());
                row.createCell(3).setCellValue(p.getLocalisation());
                row.createCell(4).setCellValue(p.getRegion());
                row.createCell(5).setCellValue(p.getMesure());
                row.createCell(6).setCellValue(p.getQuantite());
                row.createCell(7).setCellValue(p.getCoutAchat());
                row.createCell(8).setCellValue(p.getValeur());
                row.createCell(9).setCellValue(p.getDateExpiry() != null ? p.getDateExpiry().toString() : "");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Exporter les produits expirés");
            fileChooser.setInitialFileName("Produits_Expirés_" + System.currentTimeMillis() + ".xlsx");
            fileChooser.getExtensionFilters()
                    .add(new javafx.stage.FileChooser.ExtensionFilter("Fichier Excel", "*.xlsx"));
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void exportXlsFinancialStates(data.finance.BilanReport bilan, data.finance.CompteResultatReport cr,
            double chargesInd, String entrepriseName) {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Sheet 1: Bilan
            org.apache.poi.ss.usermodel.Sheet sheet1 = workbook.createSheet("Bilan");
            String[] bilanCols = { "Rubrique", "Montant (USD)" };
            Row header1 = sheet1.createRow(0);
            for (int i = 0; i < bilanCols.length; i++) {
                header1.createCell(i).setCellValue(bilanCols[i]);
            }

            int r1 = 1;
            sheet1.createRow(r1++).createCell(0).setCellValue("ACTIF NON COURANT");
            sheet1.getRow(r1 - 1).createCell(1).setCellValue(bilan.getActifNonCourant());
            sheet1.createRow(r1++).createCell(0).setCellValue("ACTIF COURANT");
            sheet1.getRow(r1 - 1).createCell(1).setCellValue(bilan.getActifCourant());
            sheet1.createRow(r1++).createCell(0).setCellValue("TOTAL ACTIF");
            sheet1.getRow(r1 - 1).createCell(1).setCellValue(bilan.getTotalActif());
            sheet1.createRow(r1++).createCell(0).setCellValue("");
            sheet1.createRow(r1++).createCell(0).setCellValue("CAPITAUX PROPRES");
            sheet1.getRow(r1 - 1).createCell(1).setCellValue(bilan.getCapitauxPropres());
            sheet1.createRow(r1++).createCell(0).setCellValue("DETTES (COURANT + NON COURANT)");
            sheet1.getRow(r1 - 1).createCell(1).setCellValue(bilan.getPassifCourant() + bilan.getPassifNonCourant());
            sheet1.createRow(r1++).createCell(0).setCellValue("TOTAL PASSIF");
            sheet1.getRow(r1 - 1).createCell(1).setCellValue(bilan.getTotalPassif());

            // Sheet 2: Compte de Résultat
            org.apache.poi.ss.usermodel.Sheet sheet2 = workbook.createSheet("Compte de Résultat");
            String[] crCols = { "Poste", "Valeur (USD)" };
            Row header2 = sheet2.createRow(0);
            for (int i = 0; i < crCols.length; i++) {
                header2.createCell(i).setCellValue(crCols[i]);
            }

            int r2 = 1;
            sheet2.createRow(r2++).createCell(0).setCellValue("CHIFFRE D'AFFAIRES");
            sheet2.getRow(r2 - 1).createCell(1).setCellValue(cr.getChiffreAffaires());
            sheet2.createRow(r2++).createCell(0).setCellValue("COUT DES VENTES");
            sheet2.getRow(r2 - 1).createCell(1).setCellValue(cr.getCoutDesVentes());
            sheet2.createRow(r2++).createCell(0).setCellValue("MARGE BRUTE");
            sheet2.getRow(r2 - 1).createCell(1).setCellValue(cr.getMargeBrute());
            sheet2.createRow(r2++).createCell(0).setCellValue("DEPENSES OPERATIONNELLES");
            sheet2.getRow(r2 - 1).createCell(1).setCellValue(cr.getDepensesOperationnelles());
            sheet2.createRow(r2++).createCell(0).setCellValue("AMORTISSEMENTS");
            sheet2.getRow(r2 - 1).createCell(1).setCellValue(cr.getAmortissements());
            sheet2.createRow(r2++).createCell(0).setCellValue("VARIATION DE STOCK");
            sheet2.getRow(r2 - 1).createCell(1).setCellValue(cr.getVariationStock());
            sheet2.createRow(r2++).createCell(0).setCellValue("RESULTAT D'EXPLOITATION");
            sheet2.getRow(r2 - 1).createCell(1).setCellValue(cr.getResultatExploitation());
            sheet2.createRow(r2++).createCell(0).setCellValue("IMPOTS ESTIMES");
            sheet2.getRow(r2 - 1).createCell(1).setCellValue(cr.getImpotsEstimes());
            sheet2.createRow(r2++).createCell(0).setCellValue("RESULTAT NET");
            sheet2.getRow(r2 - 1).createCell(1).setCellValue(cr.getResultatNet());

            // Sheet 3: Flux de Trésorerie
            org.apache.poi.ss.usermodel.Sheet sheet3 = workbook.createSheet("Flux de Trésorerie");
            String[] fluxCols = { "Libellé", "Montant (USD)" };
            Row header3 = sheet3.createRow(0);
            for (int i = 0; i < fluxCols.length; i++) {
                header3.createCell(i).setCellValue(fluxCols[i]);
            }

            double encaissement = cr.getChiffreAffaires();
            double decaissement = Math.max(0d, cr.getCoutDesVentes()) + Math.max(0d, cr.getDepensesOperationnelles())
                    + Math.max(0d, cr.getAmortissements()) + Math.max(0d, chargesInd);
            double fluxNet = encaissement - decaissement;

            int r3 = 1;
            sheet3.createRow(r3++).createCell(0).setCellValue("ENCAISSEMENTS");
            sheet3.getRow(r3 - 1).createCell(1).setCellValue(encaissement);
            sheet3.createRow(r3++).createCell(0).setCellValue("DECAISSEMENTS");
            sheet3.getRow(r3 - 1).createCell(1).setCellValue(decaissement);
            sheet3.createRow(r3++).createCell(0).setCellValue("FLUX NET DE TRESORERIE");
            sheet3.getRow(r3 - 1).createCell(1).setCellValue(fluxNet);

            for (int i = 0; i < 2; i++) {
                sheet1.autoSizeColumn(i);
                sheet2.autoSizeColumn(i);
                sheet3.autoSizeColumn(i);
            }

            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Exporter les états financiers");
            fileChooser.setInitialFileName(
                    "etat_financier_" + entrepriseName.replace(" ", "_") + "_" + System.currentTimeMillis() + ".xlsx");
            fileChooser.getExtensionFilters()
                    .add(new javafx.stage.FileChooser.ExtensionFilter("Fichier Excel", "*.xlsx"));
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void exportXlsAttendance(List<Presence> presences, String entrepriseName) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Présences Agent");

            String[] columns = { "Date/Heure", "Agent ID", "Nom", "Prénom", "Type", "Région", "Empreinte (Hash)" };

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.BLUE.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowNum = 1;
            for (Presence p : presences) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(p.getTimestamp() != null ? p.getTimestamp().toString() : "");
                row.createCell(1).setCellValue(p.getAgentId());
                row.createCell(2).setCellValue(p.getAgentNom());
                row.createCell(3).setCellValue(p.getAgentPrenom());
                row.createCell(4).setCellValue(p.getTypePresence());
                row.createCell(5).setCellValue(p.getRegion());
                row.createCell(6).setCellValue(p.getFingerprintHash());
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le rapport de présence");
            fileChooser.setInitialFileName(
                    "presenceagents_" + entrepriseName.toLowerCase().replaceAll(" ", "_") + ".xlsx");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));

            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                }
                Desktop.getDesktop().open(file);
            }
            workbook.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void exportXlsClientStatement(Client c, List<Vente> debts) {
        try (Workbook workbook = new XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Relevé de Dette - " + c.getNomClient());

            Row r0 = sheet.createRow(0);
            r0.createCell(0).setCellValue("Client:");
            r0.createCell(1).setCellValue(c.getNomClient());

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Téléphone:");
            r1.createCell(1).setCellValue(c.getPhone());

            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("Adresse:");
            r2.createCell(1).setCellValue(c.getAdresse());

            Row r3 = sheet.createRow(3);
            r3.createCell(0).setCellValue("Date du Relevé:");
            r3.createCell(1).setCellValue(LocalDate.now().toString());

            String[] columns = { "Date", "Num Facture", "Libellé", "Net à Payer (USD)", "Déjà Payé (USD)",
                    "Reste (USD)" };

            Row headerRow = sheet.createRow(5);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerCellStyle.setFont(headerFont);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowNum = 6;
            double totalDebt = 0;
            for (Vente v : debts) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0)
                        .setCellValue(v.getDateVente() != null ? v.getDateVente().toLocalDate().toString() : "");
                row.createCell(1).setCellValue(v.getReference());
                row.createCell(2).setCellValue(v.getLibelle());
                row.createCell(3).setCellValue(v.getMontantUsd());
                double payed = v.getMontantUsd() - v.getMontantDette();
                row.createCell(4).setCellValue(payed);
                row.createCell(5).setCellValue(v.getMontantDette());
                totalDebt += v.getMontantDette();
            }

            Row footRow = sheet.createRow(rowNum + 1);
            Cell footLabel = footRow.createCell(4);
            footLabel.setCellValue("TOTAL DETTE:");
            CellStyle footStyle = workbook.createCellStyle();
            Font footFont = workbook.createFont();
            footFont.setBold(true);
            footStyle.setFont(footFont);
            footLabel.setCellStyle(footStyle);
            Cell footVal = footRow.createCell(5);
            footVal.setCellValue(totalDebt);
            footVal.setCellStyle(footStyle);

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Exporter le relevé client");
            fileChooser.setInitialFileName(
                    "Releve_" + c.getNomClient().replaceAll(" ", "_") + "_" + System.currentTimeMillis() + ".xlsx");
            fileChooser.getExtensionFilters()
                    .add(new javafx.stage.FileChooser.ExtensionFilter("Fichier Excel", "*.xlsx"));
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File exportPdfClientStatement(Client c, List<Vente> debts, Entreprise entrep) {
        try {
            double taux_dechange = pref.getDouble("taux2change", 2350);
            String devise_symbole = pref.get("mainCur", "USD");
            String path = System.getProperty("user.home") + "/Documents/Kazisafex/Reports";
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(path + "/Etat_Dette_Client_" + c.getNomClient().replaceAll(" ", "_") + "_"
                    + Constants.TIMESTAMPED_FORMAT.format(new Date()) + ".pdf");
            System.out.println("file = " + file.getAbsolutePath());
            try (PDDocument document = new PDDocument()) {
                PDPage fPage = new PDPage(PDRectangle.A4);
                document.addPage(fPage);

                int pageW = (int) PDRectangle.A4.getWidth();
                int pageH = (int) PDRectangle.A4.getHeight();

                PDPageContentStream contentStream = new PDPageContentStream(document, fPage);
                try {
                    PDFUtils pdf = new PDFUtils(document, contentStream);

                    PDFont hnormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                    PDFont hbold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

                    java.awt.Color endeleya = new java.awt.Color(68, 206, 245);
                    java.awt.Color egray = new java.awt.Color(218, 218, 219);

                    // Logo optimization
                    try {
                        File flogo = FileUtils.pointFile(entrep.getUid() + ".png");
                        PDImageXObject logo = null;
                        if (flogo.exists()) {
                            logo = PDImageXObject.createFromFile(flogo.getPath(), document);
                        } else {
                            InputStream is = MainuiController.class.getResourceAsStream("/icons/gallery.png");
                            if (is != null) {
                                byte[] bytes = IOUtils.toByteArray(is);
                                logo = PDImageXObject.createFromByteArray(document, bytes, "logo");
                            }
                        }
                        if (logo != null) {
                            contentStream.drawImage(logo, pageW - 114, pageH - 114, 84, 84);
                        }
                    } catch (Exception e) {
                        // Silent logo failure
                    }

                    pdf.addTextLine("Etat de Dette Client", 25, pageH - 98, hbold, 30, java.awt.Color.DARK_GRAY);

                    contentStream.setStrokingColor(endeleya);
                    contentStream.setLineWidth(2);
                    contentStream.moveTo(25, pageH - 120);
                    contentStream.lineTo(pageW - 25, pageH - 120);
                    contentStream.stroke();

                    // Business Info
                    pdf.addTextLine(entrep.getNomEntreprise(), 25, pageH - 150, hnormal, 16, java.awt.Color.BLACK);
                    pdf.addTextLine(new String[] {
                            "Adresse : " + entrep.getAdresse(),
                            "RCCM : " + entrep.getIdentification(),
                            entrep.getIdNat() == null ? "" : "ID-NAT : " + entrep.getIdNat(),
                            entrep.getNumeroImpot() == null ? "" : "NIF : " + entrep.getNumeroImpot()
                    }, 15, 25, pageH - 165, hnormal, 11, java.awt.Color.BLACK);

                    // Client Info
                    pdf.addTextLine("Client : " + c.getNomClient(), 25, pageH - 240, hbold, 14, java.awt.Color.BLACK);
                    pdf.addTextLine(new String[] {
                            "Téléphone : " + c.getPhone(),
                            "Adresse : " + c.getAdresse()
                    }, 15, 25, pageH - 255, hnormal, 11, java.awt.Color.BLACK);

                    String dateStr = "Date : " + Constants.DATE_HEURE_USER_READABLE_FORMAT.format(new Date());
                    pdf.addTextLine(dateStr, ((int) (pageW - hnormal.getStringWidth(dateStr) / 1000 * 11 - 32)),
                            pageH - 240, hnormal, 11, java.awt.Color.BLACK);

                    // Table Header
                    int table[] = { 80, 100, 130, 80, 80, 80 };
                    pdf.addTable(table, 25, 25, pageH - 320);
                    pdf.setFont(hbold, 10, java.awt.Color.WHITE);
                    pdf.setRightAlignedColumns(new int[] { 3, 4, 5 });

                    pdf.addCell("Date", endeleya);
                    pdf.addCell("N# Facture", endeleya);
                    pdf.addCell("Libellé", endeleya);
                    pdf.addCell("Net à Payer", endeleya);
                    pdf.addCell("Déjà Payé", endeleya);
                    pdf.addCell("Reste", endeleya);

                    // Table Body
                    pdf.setFont(hnormal, 9, java.awt.Color.BLACK);
                    double totalDebt = 0;
                    int i = 0;
                    int ln = 0;
                    int lpp = 26;
                    for (Vente v : debts) {
                        i++;
                        ln++;
                        if (i > 13) {
                            if (i == 14 || ln == lpp) {
                                contentStream.close();
                                PDPage fPage2 = new PDPage(PDRectangle.A4);
                                document.addPage(fPage2);
                                contentStream = new PDPageContentStream(document, fPage2);
                                pdf = new PDFUtils(document, contentStream);
                                pdf.addTable(table, 25, 25, pageH - 68);
                                pdf.setFont(hnormal, 9, java.awt.Color.BLACK);
                                if (ln == lpp || i == 14) {
                                    ln = 0;
                                }
                            }
                        }

                        pdf.setRightAlignedColumns(new int[] { 3, 4, 5 });
                        pdf.addCell(v.getDateVente().toLocalDate().toString(), egray);
                        pdf.addCell(v.getReference(), egray);
                        pdf.addCell(v.getLibelle(), egray);
                        double topay = devise_symbole.equals("USD")
                                ? (v.getMontantUsd() + (v.getMontantCdf() / taux_dechange))
                                : ((v.getMontantUsd() * taux_dechange) + v.getMontantCdf());
                        topay = topay + v.getMontantDette();
                        pdf.addCell(String.format("%.2f", topay), egray);
                        double payed = devise_symbole.equals("USD")
                                ? (v.getMontantUsd() + (v.getMontantCdf() / taux_dechange))
                                : ((v.getMontantUsd() * taux_dechange) + v.getMontantCdf());
                        payed = BigDecimal.valueOf(payed)
                                .setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
                        pdf.addCell(String.format("%.2f", payed), egray);
                        pdf.addCell(String.format("%.2f", v.getMontantDette()), egray);
                        totalDebt += v.getMontantDette();
                    }

                    // Summary
                    pdf.addCell("", null);
                    pdf.addCell("", null);
                    pdf.addCell("TOTAL", endeleya);
                    pdf.addCell("", endeleya);
                    pdf.addCell("", endeleya);
                    pdf.addCell(String.format("%.2f USD", totalDebt), endeleya);
                } finally {
                    if (contentStream != null) {
                        contentStream.close();
                    }
                }
                document.save(file);
            }
            return file;
        } catch (Exception e) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
    }

    public static File exportPdfSupplierStatement(Fournisseur f, List<Livraison> debts, Entreprise entrep) {
        try {
            String path = System.getProperty("user.home") + "/Documents/Kazisafex/Reports";
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(path + "/Etat_Dette_Fournisseur_" + f.getNomFourn().replaceAll(" ", "_") + "_"
                    + Constants.TIMESTAMPED_FORMAT.format(new Date()) + ".pdf");

            try (PDDocument document = new PDDocument()) {
                PDPage fPage = new PDPage(PDRectangle.A4);
                document.addPage(fPage);

                int pageW = (int) PDRectangle.A4.getWidth();
                int pageH = (int) PDRectangle.A4.getHeight();

                PDPageContentStream contentStream = new PDPageContentStream(document, fPage);
                try {
                    PDFUtils pdf = new PDFUtils(document, contentStream);

                    PDFont hnormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                    PDFont hbold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

                    java.awt.Color endeleya = new java.awt.Color(68, 206, 245);
                    java.awt.Color egray = new java.awt.Color(218, 218, 219);

                    // Logo optimization
                    try {
                        File flogo = FileUtils.pointFile(entrep.getUid() + ".png");
                        PDImageXObject logo = null;
                        if (flogo.exists()) {
                            logo = PDImageXObject.createFromFile(flogo.getPath(), document);
                        } else {
                            InputStream is = MainuiController.class.getResourceAsStream("/icons/gallery.png");
                            if (is != null) {
                                byte[] bytes = IOUtils.toByteArray(is);
                                logo = PDImageXObject.createFromByteArray(document, bytes, "logo");
                            }
                        }
                        if (logo != null) {
                            contentStream.drawImage(logo, pageW - 114, pageH - 114, 84, 84);
                        }
                    } catch (Exception e) {
                        // Silent logo failure
                    }

                    pdf.addTextLine("Etat de Dette Fournisseur", 25, pageH - 98, hbold, 30, java.awt.Color.DARK_GRAY);

                    contentStream.setStrokingColor(endeleya);
                    contentStream.setLineWidth(2);
                    contentStream.moveTo(25, pageH - 120);
                    contentStream.lineTo(pageW - 25, pageH - 120);
                    contentStream.stroke();

                    // Business Info
                    pdf.addTextLine(entrep.getNomEntreprise(), 25, pageH - 150, hnormal, 16, java.awt.Color.BLACK);
                    pdf.addTextLine(new String[] {
                            "Adresse : " + entrep.getAdresse(),
                            "RCCM : " + entrep.getIdentification(),
                            entrep.getIdNat() == null ? "" : "ID-NAT : " + entrep.getIdNat(),
                            entrep.getNumeroImpot() == null ? "" : "NIF : " + entrep.getNumeroImpot()
                    }, 15, 25, pageH - 165, hnormal, 11, java.awt.Color.BLACK);

                    // Supplier Info
                    pdf.addTextLine("Fournisseur : " + f.getNomFourn(), 25, pageH - 240, hbold, 14,
                            java.awt.Color.BLACK);
                    pdf.addTextLine(new String[] {
                            "Téléphone : " + f.getPhone(),
                            "Adresse : " + f.getAdresse()
                    }, 15, 25, pageH - 255, hnormal, 11, java.awt.Color.BLACK);

                    String dateStr = "Date : " + Constants.DATE_HEURE_USER_READABLE_FORMAT.format(new Date());
                    pdf.addTextLine(dateStr, ((int) (pageW - hnormal.getStringWidth(dateStr) / 1000 * 11 - 32)),
                            pageH - 240, hnormal, 11, java.awt.Color.BLACK);

                    // Table Header
                    int table[] = { 80, 100, 130, 80, 80, 80 };
                    pdf.addTable(table, 25, 25, pageH - 320);
                    pdf.setFont(hbold, 10, java.awt.Color.WHITE);
                    pdf.setRightAlignedColumns(new int[] { 3, 4, 5 });

                    pdf.addCell("Date", endeleya);
                    pdf.addCell("Num Piece", endeleya);
                    pdf.addCell("Libellé", endeleya);
                    pdf.addCell("Facturé", endeleya);
                    pdf.addCell("Déjà Payé", endeleya);
                    pdf.addCell("Reste", endeleya);

                    // Table Body with Pagination
                    pdf.setFont(hnormal, 9, java.awt.Color.BLACK);
                    double totalDebt = 0;
                    int i = 0;
                    int ln = 0;
                    int lpp = 26; // Lines per full page

                    for (Livraison l : debts) {
                        i++;
                        ln++;
                        if (i > 13) { // 14 rows on first page
                            if (i == 14 || ln == lpp) {
                                contentStream.close();
                                PDPage fPage2 = new PDPage(PDRectangle.A4);
                                document.addPage(fPage2);
                                contentStream = new PDPageContentStream(document, fPage2);
                                pdf = new PDFUtils(document, contentStream);
                                pdf.addTable(table, 25, 25, pageH - 68);
                                pdf.setFont(hnormal, 9, java.awt.Color.BLACK);
                                if (ln == lpp || i == 14) {
                                    ln = 0;
                                }
                            }
                        }

                        pdf.setRightAlignedColumns(new int[] { 3, 4, 5 });
                        pdf.addCell(l.getDateLivr() != null ? l.getDateLivr().toString() : "", egray);
                        pdf.addCell(l.getNumPiece(), egray);
                        pdf.addCell(l.getLibelle(), egray);
                        pdf.addCell(String.format("%.2f", l.getTopay() != null ? l.getTopay() : 0.0), egray);
                        pdf.addCell(String.format("%.2f", l.getPayed() != null ? l.getPayed() : 0.0), egray);
                        pdf.addCell(String.format("%.2f", l.getRemained() != null ? l.getRemained() : 0.0), egray);
                        totalDebt += (l.getRemained() != null ? l.getRemained() : 0.0);
                    }

                    // Total Row
                    pdf.addCell("", null);
                    pdf.addCell("", null);
                    pdf.addCell("", null);
                    pdf.addCell("TOTAL", endeleya);
                    pdf.addCell("", endeleya);
                    pdf.addCell(String.format("%.2f USD", totalDebt), endeleya);
                } finally {
                    if (contentStream != null) {
                        contentStream.close();
                    }
                }
                document.save(file);
            }
            return file;
        } catch (Exception e) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
    }

    public static File exportPdfSuppliersDebt(List<Fournisseur> suppliers, Entreprise entrep) {
        try {
            String path = System.getProperty("user.home") + "/Documents/Kazisafex/Reports";
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(path + "/Dettes_Fournisseurs_Global_"
                    + Constants.TIMESTAMPED_FORMAT.format(new Date()) + ".pdf");

            try (PDDocument document = new PDDocument()) {
                PDPage fPage = new PDPage(PDRectangle.A4);
                document.addPage(fPage);

                int pageW = (int) PDRectangle.A4.getWidth();
                int pageH = (int) PDRectangle.A4.getHeight();

                PDPageContentStream contentStream = new PDPageContentStream(document, fPage);
                try {
                    PDFUtils pdf = new PDFUtils(document, contentStream);

                    PDFont hnormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                    PDFont hbold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

                    java.awt.Color endeleya = new java.awt.Color(68, 206, 245);
                    java.awt.Color egray = new java.awt.Color(218, 218, 219);

                    // Logo
                    try {
                        File flogo = FileUtils.pointFile(entrep.getUid() + ".png");
                        PDImageXObject logo = null;
                        if (flogo.exists()) {
                            logo = PDImageXObject.createFromFile(flogo.getPath(), document);
                        } else {
                            InputStream is = MainuiController.class.getResourceAsStream("/icons/gallery.png");
                            if (is != null) {
                                byte[] bytes = IOUtils.toByteArray(is);
                                logo = PDImageXObject.createFromByteArray(document, bytes, "logo");
                            }
                        }
                        if (logo != null) {
                            contentStream.drawImage(logo, pageW - 114, pageH - 114, 84, 84);
                        }
                    } catch (Exception e) {
                    }

                    pdf.addTextLine("Liste des Dettes Fournisseurs", 25, pageH - 98, hbold, 28, java.awt.Color.DARK_GRAY);

                    contentStream.setStrokingColor(endeleya);
                    contentStream.setLineWidth(2);
                    contentStream.moveTo(25, pageH - 120);
                    contentStream.lineTo(pageW - 25, pageH - 120);
                    contentStream.stroke();

                    // Business Info
                    pdf.addTextLine(entrep.getNomEntreprise(), 25, pageH - 150, hnormal, 16, java.awt.Color.BLACK);
                    pdf.addTextLine(new String[] {
                            "Adresse : " + entrep.getAdresse(),
                            "RCCM : " + entrep.getIdentification()
                    }, 15, 25, pageH - 165, hnormal, 11, java.awt.Color.BLACK);

                    String dateStr = "Date : " + Constants.DATE_HEURE_USER_READABLE_FORMAT.format(new Date());
                    pdf.addTextLine(dateStr, ((int) (pageW - hnormal.getStringWidth(dateStr) / 1000 * 11 - 32)),
                            pageH - 240, hnormal, 11, java.awt.Color.BLACK);

                    // Table Header
                    int table[] = { 150, 150, 100, 120 };
                    pdf.addTable(table, 25, 25, pageH - 320);
                    pdf.setFont(hbold, 10, java.awt.Color.WHITE);
                    pdf.setRightAlignedColumns(new int[] { 3 });

                    pdf.addCell("Nom Fournisseur", endeleya);
                    pdf.addCell("Adresse", endeleya);
                    pdf.addCell("Téléphone", endeleya);
                    pdf.addCell("Dette Totale", endeleya);

                    // Table Body with Pagination
                    pdf.setFont(hnormal, 9, java.awt.Color.BLACK);
                    double globalTotal = 0;
                    int i = 0;
                    int ln = 0;
                    int lpp = 26;

                    for (Fournisseur f : suppliers) {
                        double totalDebt = f.getLivraisonList() == null ? 0d
                                : f.getLivraisonList().stream()
                                        .mapToDouble(l -> l.getRemained() != null ? l.getRemained() : 0.0).sum();
                        if (totalDebt <= 0)
                            continue;

                        i++;
                        ln++;
                        if (i > 13) {
                            if (i == 14 || ln == lpp) {
                                contentStream.close();
                                PDPage fPage2 = new PDPage(PDRectangle.A4);
                                document.addPage(fPage2);
                                contentStream = new PDPageContentStream(document, fPage2);
                                pdf = new PDFUtils(document, contentStream);
                                pdf.addTable(table, 25, 25, pageH - 68);
                                pdf.setFont(hnormal, 9, java.awt.Color.BLACK);
                                if (ln == lpp || i == 14) {
                                    ln = 0;
                                }
                            }
                        }

                        pdf.setRightAlignedColumns(new int[] { 3 });
                        pdf.addCell(f.getNomFourn(), egray);
                        pdf.addCell(f.getAdresse(), egray);
                        pdf.addCell(f.getPhone(), egray);
                        pdf.addCell(String.format("%.2f USD", totalDebt), egray);
                        globalTotal += totalDebt;
                    }

                    // Final Total
                    pdf.addCell("", null);
                    pdf.addCell("", null);
                    pdf.addCell("TOTAL GLOBAL", endeleya);
                    pdf.addCell(String.format("%.2f USD", globalTotal), endeleya);
                } finally {
                    if (contentStream != null) {
                        contentStream.close();
                    }
                }
                document.save(file);
            }
            return file;
        } catch (Exception e) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
    }

}
