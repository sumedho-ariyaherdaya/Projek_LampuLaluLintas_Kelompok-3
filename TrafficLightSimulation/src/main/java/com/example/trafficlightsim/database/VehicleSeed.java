package com.example.trafficlightsim.database;

import static com.example.trafficlightsim.model.JenisKendaraan.MOBIL;
import static com.example.trafficlightsim.model.JenisKendaraan.SEPEDA_MOTOR;
import static com.example.trafficlightsim.model.WarnaKendaraan.*;

import com.example.trafficlightsim.model.JenisKendaraan;
import com.example.trafficlightsim.model.VehicleIdentity;
import com.example.trafficlightsim.model.WarnaKendaraan;
import java.util.List;

/**
 * Daftar 20 kendaraan (10 mobil, 10 sepeda motor). Dimasukkan ke database sekali saja,
 * saat tabel kendaraan masih kosong. Kombinasi jenis + warna + merek harus unik.
 */
final class VehicleSeed {
    private VehicleSeed() { }

    static List<VehicleIdentity> all() {
        return List.of(
            v("B19N", "Tasya Maharani Putri", MOBIL, MERAH, "AvnZ"),
            v("V74V", "Ardi Pratama", SEPEDA_MOTOR, BIRU, "HND"),
            v("Q83X", "Aldi Firmansyah", MOBIL, KUNING, "TYT"),
            v("J52N", "Alya Putri Maharani", SEPEDA_MOTOR, UNGU, "Vrio"),
            v("D32J", "Rizky Ramadhan", MOBIL, HIJAU, "innoV"),
            v("A73H", "Syifa Nur Azzahra", SEPEDA_MOTOR, OREN, "gennie"),
            v("M45I", "Andi Wijaya", MOBIL, BIRU, "AvnZ"),
            v("Z96J", "Reza Hidayat", SEPEDA_MOTOR, MERAH, "HND"),
            v("C84Z", "Rendy Saputra", MOBIL, UNGU, "TYT"),
            v("P96A", "Citra Lestari", SEPEDA_MOTOR, KUNING, "Vrio"),
            v("R42T", "Yoga Saputra", MOBIL, OREN, "innoV"),
            v("N84Z", "Rina Oktaviani", SEPEDA_MOTOR, HIJAU, "gennie"),
            v("H66W", "Reza Firmansyah", MOBIL, MERAH, "TYT"),
            v("L42G", "Siti Nurhaliza", SEPEDA_MOTOR, BIRU, "Vrio"),
            v("W88M", "Aditya Firmansyah", MOBIL, HIJAU, "AvnZ"),
            v("F65O", "Muhammad Iqbal", SEPEDA_MOTOR, UNGU, "HND"),
            v("Q54Y", "Dinda Ayu Permata", MOBIL, KUNING, "innoV"),
            // Daftar asli: Oren - gennie, kembar dengan A73H (Syifa). Merek diganti HND agar ciri tetap unik.
            v("K87H", "Fadli Akbar", SEPEDA_MOTOR, OREN, "HND"),
            v("E96A", "Putri Amelia", MOBIL, BIRU, "TYT"),
            v("U38R", "Fajar Nugraha", SEPEDA_MOTOR, MERAH, "Vrio"));
    }

    private static VehicleIdentity v(String plate, String owner, JenisKendaraan kind, WarnaKendaraan color, String brand) {
        return new VehicleIdentity(plate, owner, kind, color, brand);
    }
}
