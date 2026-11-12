package com.example.trafficlightsim.view;

import java.awt.Dialog;
import javax.swing.*;

/** Hanya untuk melihat tampilan: klik kanan > Run File di NetBeans. Tidak dipakai aplikasi. */
public class ViewPreview {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame list = new JFrame("Pratinjau - Daftar Pelanggaran");
            ViolationListPanel panel = new ViolationListPanel();
            panel.setRecords(PreviewData.records());
            list.setContentPane(panel);
            list.setSize(1100, 640);
            list.setLocation(30, 30);
            list.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            list.setVisible(true);

            InputDataDialog input = new InputDataDialog(list);
            input.setModalityType(Dialog.ModalityType.MODELESS);
            input.setLocation(60, 90);
            input.setVisible(true);

            ReceiptDialog receipt = new ReceiptDialog(list);
            receipt.setModalityType(Dialog.ModalityType.MODELESS);
            receipt.setRecord(PreviewData.records().get(0));
            receipt.setLocation(620, 120);
            receipt.setVisible(true);
        });
    }
}
