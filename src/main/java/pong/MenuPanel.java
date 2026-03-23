package pong;

import pong.i18n.Lang;
import pong.util.GameConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * The main menu panel.
 *
 * <p>Shows four groups of radio buttons (language, game mode, difficulty,
 * window size) and a "Start Game" button.  Difficulty options are automatically
 * greyed out when "2 Players" is selected; they become active again when
 * "vs. Computer" is chosen.  Window-size presets that exceed the usable screen
 * area are automatically disabled (greyed out).  Switching the language
 * instantly re-labels every component.</p>
 */
public class MenuPanel extends JPanel {

    /**
     * Carries the user's final selection back to the caller.
     *
     * @param mode       chosen game mode
     * @param difficulty chosen difficulty (only meaningful for VS_COMPUTER)
     * @param lang       chosen language
     * @param preset     chosen window-size preset
     */
    public record MenuResult(GameMode mode, Difficulty difficulty, Lang lang, WindowPreset preset) {}

    // ── current language ─────────────────────────────────────────────────────
    private Lang lang;

    // ── labels ────────────────────────────────────────────────────────────────
    private final JLabel lblLanguage   = new JLabel();
    private final JLabel lblMode       = new JLabel();
    private final JLabel lblDiff       = new JLabel();
    private final JLabel lblDiffNote   = new JLabel();
    private final JLabel lblWindowSize = new JLabel();

    // ── radio buttons ─────────────────────────────────────────────────────────
    private final JRadioButton rbLangEn      = new JRadioButton();
    private final JRadioButton rbLangDe      = new JRadioButton();
    private final JRadioButton rbMode2P      = new JRadioButton();
    private final JRadioButton rbModeComp    = new JRadioButton();
    private final JRadioButton rbDiffEasy    = new JRadioButton();
    private final JRadioButton rbDiffMedium  = new JRadioButton();
    private final JRadioButton rbDiffHard    = new JRadioButton();
    private final JRadioButton rbPreset1080  = new JRadioButton(WindowPreset.P1080.label);
    private final JRadioButton rbPreset1440  = new JRadioButton(WindowPreset.P1440.label);
    private final JRadioButton rbPreset4K    = new JRadioButton(WindowPreset.P4K.label);
    private final JRadioButton rbPresetSmall = new JRadioButton(WindowPreset.P_SMALL.label);

    // ── start / online buttons ────────────────────────────────────────────────
    private final JButton btnStart  = new JButton();
    private final JButton btnOnline = new JButton("Online Multiplayer (LAN)");

    // ── disabled foreground colour ────────────────────────────────────────────
    private static final Color FG_DISABLED = new Color(110, 110, 120);

    public MenuPanel(Lang initialLang, WindowPreset initialPreset,
                     GraphicsConfiguration gc, Consumer<MenuResult> onStart,
                     Runnable onOnline) {
        this.lang = initialLang;

        setBackground(GameConstants.BG);
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(40, 70, 44, 70));

        // ── title ─────────────────────────────────────────────────────────────
        JLabel title = new JLabel("PONG");
        title.setFont(new Font("SansSerif", Font.BOLD, 68));
        title.setForeground(GameConstants.ACCENT);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        // ── button groups ─────────────────────────────────────────────────────
        ButtonGroup bgLang = new ButtonGroup();
        bgLang.add(rbLangEn);
        bgLang.add(rbLangDe);
        (initialLang == Lang.EN ? rbLangEn : rbLangDe).setSelected(true);

        ButtonGroup bgMode = new ButtonGroup();
        bgMode.add(rbMode2P);
        bgMode.add(rbModeComp);
        rbMode2P.setSelected(true);

        ButtonGroup bgDiff = new ButtonGroup();
        bgDiff.add(rbDiffEasy);
        bgDiff.add(rbDiffMedium);
        bgDiff.add(rbDiffHard);
        rbDiffMedium.setSelected(true);

        ButtonGroup bgPreset = new ButtonGroup();
        bgPreset.add(rbPreset1080);
        bgPreset.add(rbPreset1440);
        bgPreset.add(rbPreset4K);
        bgPreset.add(rbPresetSmall);
        presetButton(initialPreset).setSelected(true);

        // ── shared styling ────────────────────────────────────────────────────
        Font sectionFont = new Font("SansSerif", Font.BOLD, 15);
        Font itemFont    = new Font("SansSerif", Font.PLAIN, 14);

        for (JLabel lbl : new JLabel[]{lblLanguage, lblMode, lblDiff, lblWindowSize}) {
            lbl.setForeground(GameConstants.FG);
            lbl.setFont(sectionFont);
        }
        lblDiffNote.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblDiffNote.setForeground(FG_DISABLED);

        for (JRadioButton rb : allRadioButtons()) {
            rb.setOpaque(false);
            rb.setForeground(GameConstants.FG);
            rb.setFont(itemFont);
            rb.setFocusPainted(false);
        }

        btnStart.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnStart.setBackground(GameConstants.ACCENT);
        btnStart.setForeground(GameConstants.BG);
        btnStart.setFocusPainted(false);
        btnStart.setBorderPainted(false);
        btnStart.setOpaque(true);
        btnStart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnStart.setPreferredSize(new Dimension(200, 42));

        btnOnline.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnOnline.setBackground(new Color(60, 180, 100));
        btnOnline.setForeground(GameConstants.BG);
        btnOnline.setFocusPainted(false);
        btnOnline.setBorderPainted(false);
        btnOnline.setOpaque(true);
        btnOnline.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnOnline.setPreferredSize(new Dimension(220, 38));

        // difficulty starts disabled (2 Players is the default selection)
        applyDifficultyEnabled(false);

        // disable presets that don't fit on the current screen
        applyPresetAvailability(gc);

        // apply initial text
        refreshLabels();

        // ── layout ────────────────────────────────────────────────────────────
        GridBagConstraints c = new GridBagConstraints();
        c.fill   = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        int row  = 0;

        // title (centred, extra bottom gap)
        c.gridx = 0; c.gridy = row++; c.gridwidth = 3;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(0, 0, 30, 0);
        add(title, c);
        c.anchor = GridBagConstraints.WEST;

        // ── language section ──────────────────────────────────────────────────
        c.gridx = 0; c.gridy = row++; c.gridwidth = 3;
        c.insets = sectionInsets();
        add(lblLanguage, c);

        c.gridwidth = 1;
        c.insets = itemInsets();
        c.gridx = 0; c.gridy = row;   add(rbLangEn, c);
        c.gridx = 1; c.gridy = row++; add(rbLangDe, c);

        add(separator(), separatorConstraints(row++));

        // ── game mode section ─────────────────────────────────────────────────
        c.gridx = 0; c.gridy = row++; c.gridwidth = 3;
        c.insets = sectionInsets();
        add(lblMode, c);

        c.gridwidth = 1;
        c.insets = itemInsets();
        c.gridx = 0; c.gridy = row;   add(rbMode2P, c);
        c.gridx = 1; c.gridy = row++; add(rbModeComp, c);

        add(separator(), separatorConstraints(row++));

        // ── difficulty section ────────────────────────────────────────────────
        c.gridx = 0; c.gridy = row++; c.gridwidth = 3;
        c.insets = sectionInsets();
        add(lblDiff, c);

        c.gridwidth = 1;
        c.insets = itemInsets();
        c.gridx = 0; c.gridy = row;   add(rbDiffEasy, c);
        c.gridx = 1; c.gridy = row;   add(rbDiffMedium, c);
        c.gridx = 2; c.gridy = row++; add(rbDiffHard, c);

        c.gridx = 0; c.gridy = row++; c.gridwidth = 3;
        c.insets = new Insets(0, 4, 2, 4);
        add(lblDiffNote, c);

        add(separator(), separatorConstraints(row++));

        // ── window size section ───────────────────────────────────────────────
        c.gridx = 0; c.gridy = row++; c.gridwidth = 3;
        c.insets = sectionInsets();
        add(lblWindowSize, c);

        c.gridwidth = 1;
        c.insets = itemInsets();
        c.gridx = 0; c.gridy = row;   add(rbPreset1080, c);
        c.gridx = 1; c.gridy = row;   add(rbPreset1440, c);
        c.gridx = 2; c.gridy = row++; add(rbPreset4K,   c);

        c.gridx = 0; c.gridy = row++; c.gridwidth = 1;
        c.insets = itemInsets();
        add(rbPresetSmall, c);

        // ── start button ──────────────────────────────────────────────────────
        c.gridx = 0; c.gridy = row; c.gridwidth = 3;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(22, 0, 0, 0);
        add(btnStart, c);

        // ── online multiplayer button ──────────────────────────────────────────
        c.gridy = ++row;
        c.insets = new Insets(8, 0, 0, 0);
        add(btnOnline, c);

        // ── listeners ─────────────────────────────────────────────────────────
        rbLangEn.addActionListener(e -> { lang = Lang.EN; refreshLabels(); });
        rbLangDe.addActionListener(e -> { lang = Lang.DE; refreshLabels(); });

        rbMode2P.addActionListener(e   -> applyDifficultyEnabled(false));
        rbModeComp.addActionListener(e -> applyDifficultyEnabled(true));

        btnStart.addActionListener(e -> {
            GameMode mode = rbModeComp.isSelected() ? GameMode.VS_COMPUTER : GameMode.TWO_PLAYERS;
            Difficulty diff;
            if      (rbDiffEasy.isSelected()) diff = Difficulty.EASY;
            else if (rbDiffHard.isSelected()) diff = Difficulty.HARD;
            else                              diff = Difficulty.MEDIUM;
            onStart.accept(new MenuResult(mode, diff, lang, selectedPreset()));
        });

        btnOnline.addActionListener(e -> onOnline.run());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private JRadioButton presetButton(WindowPreset preset) {
        return switch (preset) {
            case P1440   -> rbPreset1440;
            case P4K     -> rbPreset4K;
            case P_SMALL -> rbPresetSmall;
            default      -> rbPreset1080;
        };
    }

    private WindowPreset selectedPreset() {
        if (rbPreset1440.isSelected())  return WindowPreset.P1440;
        if (rbPreset4K.isSelected())    return WindowPreset.P4K;
        if (rbPresetSmall.isSelected()) return WindowPreset.P_SMALL;
        return WindowPreset.P1080;
    }

    /**
     * Disables preset radio buttons whose window size exceeds the usable screen
     * area, and ensures the current selection remains on an enabled preset.
     */
    private void applyPresetAvailability(GraphicsConfiguration gc) {
        Dimension usable = WindowPreset.usableSize(gc);

        setPresetEnabled(rbPreset1080, WindowPreset.P1080.fitsIn(usable));
        setPresetEnabled(rbPreset1440, WindowPreset.P1440.fitsIn(usable));
        setPresetEnabled(rbPreset4K,   WindowPreset.P4K.fitsIn(usable));
        // P_SMALL (900×600) is always enabled
        rbPresetSmall.setEnabled(true);
        rbPresetSmall.setForeground(GameConstants.FG);

        // If the currently selected preset is now disabled, auto-select the
        // largest one that fits.
        JRadioButton selected = presetButton(selectedPreset());
        if (!selected.isEnabled()) {
            presetButton(WindowPreset.largestFitting(usable)).setSelected(true);
        }
    }

    private void setPresetEnabled(JRadioButton rb, boolean enabled) {
        rb.setEnabled(enabled);
        rb.setForeground(enabled ? GameConstants.FG : FG_DISABLED);
    }

    private void refreshLabels() {
        lblLanguage  .setText(lang.labelLanguage()   + ":");
        rbLangEn     .setText("English");
        rbLangDe     .setText("Deutsch");
        lblMode      .setText(lang.labelGameMode()   + ":");
        rbMode2P     .setText(lang.mode2Players());
        rbModeComp   .setText(lang.modeVsComputer());
        lblDiff      .setText(lang.labelDifficulty() + ":");
        lblDiffNote  .setText(lang.diffNote());
        rbDiffEasy   .setText(lang.diffEasy());
        rbDiffMedium .setText(lang.diffMedium());
        rbDiffHard   .setText(lang.diffHard());
        lblWindowSize.setText(lang.labelWindowSize() + ":");
        btnStart     .setText(lang.btnStart());
    }

    private void applyDifficultyEnabled(boolean enabled) {
        Color fg = enabled ? GameConstants.FG : FG_DISABLED;
        lblDiff.setForeground(fg);
        for (JRadioButton rb : new JRadioButton[]{rbDiffEasy, rbDiffMedium, rbDiffHard}) {
            rb.setEnabled(enabled);
            rb.setForeground(fg);
        }
        lblDiffNote.setVisible(!enabled);
    }

    private JRadioButton[] allRadioButtons() {
        return new JRadioButton[]{
                rbLangEn, rbLangDe,
                rbMode2P, rbModeComp,
                rbDiffEasy, rbDiffMedium, rbDiffHard,
                rbPreset1080, rbPreset1440, rbPreset4K, rbPresetSmall
        };
    }

    private static Insets sectionInsets() { return new Insets(10, 4, 2,  4); }
    private static Insets itemInsets()    { return new Insets(1,  10, 1, 18); }

    private static JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(60, 60, 70));
        sep.setPreferredSize(new Dimension(0, 1));
        return sep;
    }

    private static GridBagConstraints separatorConstraints(int row) {
        GridBagConstraints sc = new GridBagConstraints();
        sc.gridx = 0; sc.gridy = row; sc.gridwidth = 3;
        sc.fill   = GridBagConstraints.HORIZONTAL;
        sc.insets = new Insets(8, 0, 4, 0);
        return sc;
    }
}
