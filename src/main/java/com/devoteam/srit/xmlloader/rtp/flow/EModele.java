/* 
 * Copyright 2012 Devoteam http://www.devoteam.com
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * 
 * This file is part of Multi-Protocol Test Suite (MTS).
 * 
 * Multi-Protocol Test Suite (MTS) is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License.
 * 
 * Multi-Protocol Test Suite (MTS) is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Multi-Protocol Test Suite (MTS).
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.devoteam.srit.xmlloader.rtp.flow;

import com.devoteam.srit.xmlloader.rtp.MsgRtp;

/**
 *
 * @author jbor
 */
public class EModele {

    //valeurs specifiques du codec
    private int SLR;		// SEND LOUDNESS RATING
    private int RLR;		// RECEIVE LOUDNESS RATING
    private int STMR;		// SIDETONE MASKING RATING
    private int LSTR;		// LISTENER SIDETONE RATING
    private int DS;		// D VALUE SENDER SIDE
    private int DR;		// D VALUE RECEIVER SIDE
    private int TELR;		// TALKER ECHO LOUDNESS RATING
    private int WEPL;		// WEIGHTED ECHO PATH LOSS
    private int QDU;		// QUANTIZATION DISTORSION UNITS
    private int IE;		// FACTEUR DE DEGRADATION DUE A L'EQUIPEMENT
    private float BPL;		// FACTEUR DE ROBUSTESSE A LA PERTE DE PAQUET
    private int BurstR;		// RAPPORT LIE AUX RAFALES
    private int NC;		// BRUIT DE CIRCUIT PAR RAPPORT AU POINT DE REFERENCE 0 dBr
    private int NFOR;		// SEUIL DE BRUIT DU COTE RECEPTION (dBmp)
    private int PS;		// BRUIT DE SALLE DU COTE EMISSION (dB)
    private int PR;		// BRUIT DE SALLE DU COTE RECEPTION (dB)
    private int A;		// ADVANTAGE FACTOR
    private double ro;		// RAPPORT SIGNAL/BRUIT
    private double no;		// SOMMATION EN PUISSANCE DES DIFFERENTS SOURCES DE BRUIT
    private double is;		// FACTEUR DE DEGRADATION SIMULTANEE
    private double iq;		// FACTEUR DE DEGRADATION DUE A LA DISTORSION DE QUANTIFICATION
    private double iolr;	// FACTEUR DE DIMINUTION DE LA QUALITE CAUSEE PAR DES VALEURS TROP FAIBLE D'OLR
    private double id;		// FACTEUR DES DEGRADATIONS DUE AU TEMPS DE PROPAGATION
    private double ie_eff;	// FACTEUR DES DEGRADATIONS DUES AUX CODECS A FAIBLE DEBIT
    private double r;
    private double mos;
    /**
     * TODO : This code has to be changed using a sliding window of 1 second for exemple
     * DO NOT ERASE IT
     */
    /*
     private LinkedList<Float> mosRT; // Real-Time mos
    */
    private String codec_name;


    //constructeur par defaut
    public EModele(){
        /*
        mosRT       = new LinkedList<Float>();
        */
    }

    //innitialisation a partir d'un modele existant
    /*actually never used*/
    public EModele(EModele m){
        SLR         = m.SLR;
        RLR         = m.RLR;
        STMR        = m.STMR;
        LSTR        = m.LSTR;
        DS          = m.DS;
        DR          = m.DR;
        TELR        = m.TELR;
        WEPL        = m.WEPL;
        QDU         = m.QDU;
        IE          = m.IE;
        BPL         = m.BPL;
        BurstR      = m.BurstR;
        NC          = m.NC;
        NFOR        = m.NFOR;
        PS          = m.PS;
        PR          = m.PR;
        A           = m.A;
        codec_name  = m.codec_name;
        /*
        mosRT       = new LinkedList<Float>();
        */
    }

    private double regleCodec(int payLoadType, double t, double delay) {
        String[] params = MosParameters.instance().getCodecParamsbyPT(payLoadType);
        if (params != null) {
            SLR         = Integer.decode(params[MosParameters.instance().SLR]);
            RLR         = Integer.decode(params[MosParameters.instance().RLR]);
            STMR        = Integer.decode(params[MosParameters.instance().STMR]);
            LSTR        = Integer.decode(params[MosParameters.instance().LSTR]);
            DS          = Integer.decode(params[MosParameters.instance().DS]);
            DR          = Integer.decode(params[MosParameters.instance().DR]);
            TELR        = Integer.decode(params[MosParameters.instance().TELR]);
            WEPL        = Integer.decode(params[MosParameters.instance().WEPL]);
            QDU         = Integer.decode(params[MosParameters.instance().QDU]);
            IE          = Integer.decode(params[MosParameters.instance().IE]);
            BPL         = Float.valueOf(params[MosParameters.instance().BPL]);
            BurstR      = Integer.decode(params[MosParameters.instance().BurstR]);
            NC          = Integer.decode(params[MosParameters.instance().NC]);
            NFOR        = Integer.decode(params[MosParameters.instance().NFOR]);
            PS          = Integer.decode(params[MosParameters.instance().PS]);
            PR          = Integer.decode(params[MosParameters.instance().PR]);
            A           = Integer.decode(params[MosParameters.instance().A]);
            codec_name  = params[MosParameters.instance().name];

            if (t == 0) {
                return 50 + (delay * 2);
            }
        }
        else
            return -1;
        return t;
    }

    /**
     * Calcul les differents parametres (no, ro, iq, iolr) du facteur R qui ne dependent ni de T ni
     * de Ppl
     */
    private void prepareCalcul() {
        // Somme des bruits, formules (2) a  (7) de G.107
        double nfo = NFOR + RLR;                                                                // formule 7
        double pre = PR + 10.0                                                                  // formule 6
            * Math.log10((1.0 + (Math.pow(10.0, (10.0 - LSTR) / 10.0))));
        double nor = RLR - 121.0 + pre + 0.008 * Math.pow((pre - 35.0), 2.0);                   // formule 5
        double nos = PS - SLR - DS - 100.0 + 0.004                                              // formule 4
            * Math.pow((PS - SLR - RLR - DS - 14.0), 2.0);
        no = 10.0 * Math.log10(Math.pow(10.0, NC / 10.0)                                        // formule 3
            + Math.pow(10.0, nos / 10.0) + Math.pow(10.0, nor / 10.0)
            + Math.pow(10.0, nfo / 10.0));
        ro = 15.0 - 1.5 * (SLR + no);                                                           // formule 2

        // Iq, formules (13) a  (17)
        double q = 37.0 - 15.0 * Math.log10(QDU);                                               // formule 17
        double g = 1.07 + 0.258 * q + 0.0602 * Math.pow(q, 2.0);                                // formule 16
        double z = 46.0 / 30.0 - g / 40.0;                                                      // formule 15
        double y = (ro - 100.0) / 15.0 + 46.0 / 8.4 - g / 9.0;                                  // formule 14
        iq = 15.0 * Math.log10(1.0 + Math.pow(10.0, y) + Math.pow(10.0, z));                    // formule 13

        // Iolr, formules (9) et (10)
        double xolr = SLR + RLR + 0.2 * (64.0 + no - RLR);                                      // formule 10
        iolr = 20.0 * (Math.pow((1.0 + Math.pow(xolr / 8.0, 8.0)), 1.0 / 8.0) - (xolr / 8.0));  // formule 9
    }

    /**
     * Calcul le facteur R
     * @param t     : delai moyen de la com. T=Ta=Tr/2, si T=0 alors on le calcul dans regleCodec
     * @param ppl   : Pourcentage de perte
     * @param tr    : Deux trames consï¿½cutives pour le calcul de T et le choix du codec
     */
//    public void calcul(double t, double ppl, MsgRtp[] tr, boolean calculRT) {
    public void calcul(double t, double ppl, int payLoadType, float delay) {
        t = regleCodec(payLoadType, t, delay);
        if (t == -1) {
            mos = -1;
            return;
        }
        prepareCalcul();

        double stmro = -10.0                                                                    // formule 12
            * Math.log10((Math.pow(10.0, -(STMR / 10.0)))
                + (Math.exp(-(t / 4.0)) * Math
                    .pow(10.0, -(TELR / 10.0))));
        double ist = 12.0                                                                       // formule 11
            * Math.pow((1.0 + Math.pow((stmro - 13.0) / 6.0, 8.0)),
                1.0 / 8.0)
            - 28.0
            * Math.pow((1.0 + Math.pow(((stmro + 1.0) / 19.4), 35.0)),
                1.0 / 35.0)
            - 13.0
            * Math.pow((1.0 + Math.pow(((stmro - 3.0) / 33.0), 13.0)),
                1.0 / 13.0) + 29.0;

        is = iolr + ist + iq;                                                                   // formule 8

        double terv = TELR - 40.0                                                               // formule 22
            * Math.log10((1.0 + t / 10.0) / (1.0 + t / 150.0)) + 6.0
            * Math.exp(-(0.3 * Math.pow(t, 2.0)));
        if (STMR < 9.0) {
            terv = terv + (ist / 2.0);
        }

        double re = 80.0 + 2.5 * (terv - 14.0);                                                 // formule 21
        double roe = -1.5 * (no - RLR);                                                         // formule 20
        double idte = (((roe - re) / 2.0)                                                       // formule 19
            + Math.sqrt((Math.pow(roe - re, 2.0)) / 4 + 100.0) - 1.0)
            * (1.0 - Math.exp(-t));
        if (STMR > 20.0) {
            idte = Math.sqrt(Math.pow(idte, 2.0) + Math.pow(ist, 2.0));
        }

        double rle = 10.5 * (WEPL + 7.0) * Math.pow(2.0 * t + 1.0, -0.25);                      // formule 26
        double idle = (ro - rle) / 2.0                                                          // formule 25
            + Math.sqrt((Math.pow(ro - rle, 2.0) / 4.0) + 169.0);

        double idd;                                                                             // formule 27
        if (t < 100.0) {
            idd = 0.0;
        }
        else {
            double x = Math.log10(t / 100.0) / Math.log10(2.0);                                 // formule 28
            idd = 25.0 * (Math.pow(1.0 + Math.pow(x, 6.0), 1.0 / 6.0) - 3.0                     // formule 27
                * (Math.pow(1.0 + Math.pow(x / 3.0, 6.0), 1.0 / 6.0)) + 2.0);
        }

        id = idte + idle + idd;                                                                 // formule 18
        // Prise en compte des pertes de paquets: Ie_eff, (cf. UIT COM12, D222,
        // formule (6))
        ie_eff = IE + ((129.0 - IE) * (ppl / ((ppl / BurstR) + BPL)));                          // formule ???
        // calcul de la note R finale , formule (1) et de la note MOS cqe
        r = ro - is - id - ie_eff + A;                                                          // formule 1
   /*     if (wb) {
            r += 36;
        }*/
        r += IE; // replacement of wideband param

//        if (!calculRT) {
           if (r < 0) {
            mos = 1;
            }
            else if (r > 100) {
                mos = 4.5;
            }
            else{
                mos = 1.0 + (0.035 * r) + (r * (r - 60.0) * (100.0 - r) * 7.0 * Math.pow(10.0, -6.0));  // annexe B-4
            } 
//        }
        /**
         * TODO : This code has to be changed using a sliding window of 1 second for exemple
         * DO NOT ERASE IT
         */
        /*
        else {
            if (r < 0) {
            mosRT.addLast((float) 1);
            }
            else if (r > 100) {
                mosRT.addLast((float) 4.5);
            }
            else{
                mosRT.addLast((float) (1.0 + (0.035 * r) + (r * (r - 60.0) * (100.0 - r) * 7.0 * Math.pow(10.0, -6.0))));
            }
        }
        */
    }

    /**
     * Retourne la valeur du champ MOS
     *
     * @return MOS
     */
    public float getMos() {
        return (float) mos;
    }

    /**
     * TODO : This code has to be changed using a sliding window of 1 second for exemple
     * DO NOT ERASE IT
     * @return
     */
    /*
    public LinkedList<Float> getMosRT(){
        return mosRT;
    }
    */

    /**
     * TODO : This code has to be changed using a sliding window of 1 second for exemple
     * DO NOT ERASE IT
     * @return
     */
    /*
    public float getMosMean(){
        float sum = 0;
        for (int i = 0; i < this.getMosRT().size(); i++){
            sum += this.getMosRT().get(i);
        }
        return sum / this.getMosRT().size();
    }
    */

    /**
     * Retourne le nom du codec utilise
     *
     * @return Nom du codec
     */
    public String getCodecName() {
        return codec_name;
    }

}