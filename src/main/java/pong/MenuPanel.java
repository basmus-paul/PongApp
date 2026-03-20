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
 * <p>Shows three groups of radio buttons (language, game mode, difficulty), a
 * fullscreen checkbox, and a "Start Game" button.  Difficulty options are
 * automatically greyed out when "2 Players" is selected; they become active
 * again when "vs. Computer" is chosen.  Switching the language instantly
 * re-labels every component.</p>
 */
public class MenuPanel extends JPanel {

    /**
     * Carries the user's final selection back to the caller.
     *
     * @param mode       chosen game mode
     * @param difficulty chosen difficulty (only meaningful for VS_COMPUTER)
     * @param lang       chosen language
     * @param fullscreen whether to launch the game in fullscreen mode
     */
    public record MenuResult(GameMode mode, Difficulty difficulty, Lang lang, boolean fullscreen) {}

    // ── current language ─────────────────────────────────────────────────────
    private Lang lang;

    // ── labels ────────────────────────────────────────────────────────────────
    private final JLabel lblLanguage  = new JLabel();
    private final JLabel lblMode      = new JLabel();
    private final JLabel lblDiff      = new JLabel();
    private final JLabel lblDiffNote  = new JLabel();

    // ── radio buttons ─────────────────────────────────────────────────────────
    private final JRadioButton rbLangEn     = new JRadioButton();
    private final JRadioButton rbLangDe     = new JRadioButton();
    private final JRadioButton rbMode2P     = new JRadioButton();
    private final JRadioButton rbModeComp   = new JRadioButton();
    private final JRadioButton rbDiffEasy   = new JRadioButton();
    private final JRadioButton rbDiffMedium = new JRadioButton();
    private final JRadioButton rbDiffHard   = new JRadioButton();

    // ── start button ─────────────────────────────────────────────────────────
    private final JButton btnStart = new JButton();

    // ── fullscreen checkbox ───────────────────────────────────────────────────
    private final JCheckBox cbFullscreen = new JCheckBox();

    // ── disabled foreground colour ────────────────────────────────────────────
    private static final Color FG_DISABLED = new Color(110, 110, 120);

    public MenuPanel(Lang initialLang, boolean initialFullscreen, Consumer<MenuResult> onStart) {
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

        cbFullscreen.setSelected(initialFullscreen);

        // ── shared styling ────────────────────────────────────────────────────
        Font sectionFont = new Font("SansSerif", Font.BOLD, 15);
        Font itemFont    = new Font("SansSerif", Font.PLAIN, 14);

        for (JLabel lbl : new JLabel[]{lblLanguage, lblMode, lblDiff}) {
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

        cbFullscreen.setOpaque(false);
        cbFullscreen.setForeground(GameConstants.FG);
        cbFullscreen.setFont(itemFont);
        cbFullscreen.setFocusPainted(false);

        btnStart.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnStart.setBackground(GameConstants.ACCENT);
        btnStart.setForeground(GameConstants.BG);
        btnStart.setFocusPainted(false);
        btnStart.setBorderPainted(false);
        btnStart.setOpaque(true);
        btnStart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnStart.setPreferredSize(new Dimension(200, 42));

        // difficulty starts disabled (2 Players is the default selection)
        applyDifficultyEnabled(false);

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

        // ── fullscreen section ────────────────────────────────────────────────
        c.gridx = 0; c.gridy = row++; c.gridwidth = 3;
        c.insets = itemInsets();
        add(cbFullscreen, c);

        // ── start button ──────────────────────────────────────────────────────
        c.gridx = 0; c.gridy = row; c.gridwidth = 3;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(22, 0, 0, 0);
        add(btnStart, c);

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
            onStart.accept(new MenuResult(mode, diff, lang, cbFullscreen.isSelected()));
        });
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void refreshLabels() {
        lblLanguage .setText(lang.labelLanguage()   + ":");
        rbLangEn    .setText("English");
        rbLangDe    .setText("Deutsch");
        lblMode     .setText(lang.labelGameMode()   + ":");
        rbMode2P    .setText(lang.mode2Players());
        rbModeComp  .setText(lang.modeVsComputer());
        lblDiff     .setText(lang.labelDifficulty() + ":");
        lblDiffNote .setText(lang.diffNote());
        rbDiffEasy  .setText(lang.diffEasy());
        rbDiffMedium.setText(lang.diffMedium());
        rbDiffHard  .setText(lang.diffHard());
        cbFullscreen.setText(lang.labelFullscreen());
        btnStart    .setText(lang.btnStart());
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
                rbDiffEasy, rbDiffMedium, rbDiffHard
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
