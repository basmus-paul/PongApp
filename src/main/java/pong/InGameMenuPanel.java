package pong;

import pong.i18n.Lang;
import pong.util.GameConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Semi-transparent in-game overlay menu shown when the player presses {@code Esc}.
 *
 * <p>The game continues to be rendered behind this panel (the glass pane).
 * The overlay lets the player:</p>
 * <ul>
 *   <li><b>Resume</b> – close the overlay and continue the current game</li>
 *   <li><b>New Game</b> – apply the chosen settings and start a fresh game</li>
 *   <li><b>Exit</b> – return to the main menu</li>
 * </ul>
 */
public class InGameMenuPanel extends JPanel {

    private Lang lang;

    // ── labels ────────────────────────────────────────────────────────────────
    private final JLabel lblLanguage = new JLabel();
    private final JLabel lblMode     = new JLabel();
    private final JLabel lblDiff     = new JLabel();
    private final JLabel lblDiffNote = new JLabel();

    // ── radio buttons ─────────────────────────────────────────────────────────
    private final JRadioButton rbLangEn     = new JRadioButton();
    private final JRadioButton rbLangDe     = new JRadioButton();
    private final JRadioButton rbMode2P     = new JRadioButton();
    private final JRadioButton rbModeComp   = new JRadioButton();
    private final JRadioButton rbDiffEasy   = new JRadioButton();
    private final JRadioButton rbDiffMedium = new JRadioButton();
    private final JRadioButton rbDiffHard   = new JRadioButton();

    // ── fullscreen checkbox ───────────────────────────────────────────────────
    private final JCheckBox cbFullscreen = new JCheckBox();

    // ── action buttons ────────────────────────────────────────────────────────
    private final JButton btnResume  = new JButton();
    private final JButton btnNewGame = new JButton();
    private final JButton btnExit    = new JButton();

    // ── title ─────────────────────────────────────────────────────────────────
    private final JLabel lblTitle = new JLabel();

    private static final Color FG_DISABLED = new Color(110, 110, 120);

    /**
     * @param initialMode       current game mode (pre-selects the radio button)
     * @param initialDifficulty current difficulty (pre-selects the radio button)
     * @param initialLang       current language
     * @param initialFullscreen current fullscreen state
     * @param onResume          invoked when the player clicks "Resume" or presses Esc
     * @param onNewGame         invoked with the chosen {@link MenuPanel.MenuResult} when the player clicks "New Game"
     * @param onExit            invoked when the player clicks "Exit"
     */
    public InGameMenuPanel(GameMode initialMode, Difficulty initialDifficulty,
                           Lang initialLang, boolean initialFullscreen,
                           Runnable onResume,
                           Consumer<MenuPanel.MenuResult> onNewGame,
                           Runnable onExit) {
        this.lang = initialLang;

        // Glass-pane: not opaque; background drawn in paintComponent
        setOpaque(false);
        setLayout(new GridBagLayout());

        // ── inner card ────────────────────────────────────────────────────────
        JPanel card = buildCard(initialMode, initialDifficulty, initialFullscreen,
                onResume, onNewGame, onExit);
        add(card);
    }

    // ── painting ──────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

    // ── card builder ──────────────────────────────────────────────────────────

    private JPanel buildCard(GameMode initialMode, Difficulty initialDifficulty,
                              boolean initialFullscreen,
                              Runnable onResume,
                              Consumer<MenuPanel.MenuResult> onNewGame,
                              Runnable onExit) {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(24, 24, 30, 235));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(30, 50, 36, 50));

        // ── button groups ─────────────────────────────────────────────────────
        ButtonGroup bgLang = new ButtonGroup();
        bgLang.add(rbLangEn);
        bgLang.add(rbLangDe);
        (lang == Lang.EN ? rbLangEn : rbLangDe).setSelected(true);

        ButtonGroup bgMode = new ButtonGroup();
        bgMode.add(rbMode2P);
        bgMode.add(rbModeComp);
        (initialMode == GameMode.TWO_PLAYERS ? rbMode2P : rbModeComp).setSelected(true);

        ButtonGroup bgDiff = new ButtonGroup();
        bgDiff.add(rbDiffEasy);
        bgDiff.add(rbDiffMedium);
        bgDiff.add(rbDiffHard);
        switch (initialDifficulty) {
            case EASY   -> rbDiffEasy.setSelected(true);
            case HARD   -> rbDiffHard.setSelected(true);
            default     -> rbDiffMedium.setSelected(true);
        }

        cbFullscreen.setSelected(initialFullscreen);

        // ── styling ───────────────────────────────────────────────────────────
        Font sectionFont = new Font("SansSerif", Font.BOLD,  14);
        Font itemFont    = new Font("SansSerif", Font.PLAIN, 13);
        Font titleFont   = new Font("SansSerif", Font.BOLD,  36);

        lblTitle.setFont(titleFont);
        lblTitle.setForeground(GameConstants.ACCENT);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        for (JLabel lbl : new JLabel[]{lblLanguage, lblMode, lblDiff}) {
            lbl.setForeground(GameConstants.FG);
            lbl.setFont(sectionFont);
        }
        lblDiffNote.setFont(new Font("SansSerif", Font.ITALIC, 11));
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

        styleActionButton(btnResume,  GameConstants.FG,     GameConstants.BG);
        styleActionButton(btnNewGame, GameConstants.ACCENT,  GameConstants.BG);
        styleActionButton(btnExit,    new Color(200, 70, 70), Color.WHITE);

        applyDifficultyEnabled(initialMode == GameMode.VS_COMPUTER);
        refreshLabels();

        // ── layout ────────────────────────────────────────────────────────────
        GridBagConstraints c = new GridBagConstraints();
        c.fill   = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        int row  = 0;

        // title
        c.gridx = 0; c.gridy = row++; c.gridwidth = 3;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(0, 0, 18, 0);
        card.add(lblTitle, c);
        c.anchor = GridBagConstraints.WEST;

        // language
        c.gridx = 0; c.gridy = row++; c.gridwidth = 3;
        c.insets = sectionInsets();
        card.add(lblLanguage, c);
        c.gridwidth = 1;
        c.insets = itemInsets();
        c.gridx = 0; c.gridy = row;   card.add(rbLangEn, c);
        c.gridx = 1; c.gridy = row++; card.add(rbLangDe, c);
        card.add(separator(), separatorConstraints(row++));

        // mode
        c.gridx = 0; c.gridy = row++; c.gridwidth = 3;
        c.insets = sectionInsets();
        card.add(lblMode, c);
        c.gridwidth = 1;
        c.insets = itemInsets();
        c.gridx = 0; c.gridy = row;   card.add(rbMode2P, c);
        c.gridx = 1; c.gridy = row++; card.add(rbModeComp, c);
        card.add(separator(), separatorConstraints(row++));

        // difficulty
        c.gridx = 0; c.gridy = row++; c.gridwidth = 3;
        c.insets = sectionInsets();
        card.add(lblDiff, c);
        c.gridwidth = 1;
        c.insets = itemInsets();
        c.gridx = 0; c.gridy = row;   card.add(rbDiffEasy, c);
        c.gridx = 1; c.gridy = row;   card.add(rbDiffMedium, c);
        c.gridx = 2; c.gridy = row++; card.add(rbDiffHard, c);
        c.gridx = 0; c.gridy = row++; c.gridwidth = 3;
        c.insets = new Insets(0, 4, 2, 4);
        card.add(lblDiffNote, c);
        card.add(separator(), separatorConstraints(row++));

        // fullscreen
        c.gridx = 0; c.gridy = row++; c.gridwidth = 3;
        c.insets = itemInsets();
        card.add(cbFullscreen, c);

        // button row
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(20, 6, 0, 6);
        c.gridx = 0; c.gridy = row; card.add(btnResume,  c);
        c.gridx = 1; c.gridy = row; card.add(btnNewGame, c);
        c.gridx = 2; c.gridy = row; card.add(btnExit,    c);

        // ── listeners ─────────────────────────────────────────────────────────
        rbLangEn.addActionListener(e -> { lang = Lang.EN; refreshLabels(); });
        rbLangDe.addActionListener(e -> { lang = Lang.DE; refreshLabels(); });
        rbMode2P.addActionListener(e   -> applyDifficultyEnabled(false));
        rbModeComp.addActionListener(e -> applyDifficultyEnabled(true));

        btnResume.addActionListener(e -> onResume.run());

        btnNewGame.addActionListener(e -> {
            GameMode   mode = rbModeComp.isSelected() ? GameMode.VS_COMPUTER : GameMode.TWO_PLAYERS;
            Difficulty diff;
            if      (rbDiffEasy.isSelected()) diff = Difficulty.EASY;
            else if (rbDiffHard.isSelected()) diff = Difficulty.HARD;
            else                              diff = Difficulty.MEDIUM;
            onNewGame.accept(new MenuPanel.MenuResult(mode, diff, lang, cbFullscreen.isSelected()));
        });

        btnExit.addActionListener(e -> onExit.run());

        return card;
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void refreshLabels() {
        lblTitle    .setText(lang.inGameMenuTitle());
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
        btnResume   .setText(lang.btnResume());
        btnNewGame  .setText(lang.btnNewGame());
        btnExit     .setText(lang.btnExit());
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

    private static void styleActionButton(JButton btn, Color bg, Color fg) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 36));
    }

    private JRadioButton[] allRadioButtons() {
        return new JRadioButton[]{
                rbLangEn, rbLangDe,
                rbMode2P, rbModeComp,
                rbDiffEasy, rbDiffMedium, rbDiffHard
        };
    }

    private static Insets sectionInsets() { return new Insets(8, 4, 2, 4); }
    private static Insets itemInsets()    { return new Insets(1, 10, 1, 14); }

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
        sc.insets = new Insets(6, 0, 3, 0);
        return sc;
    }
}
