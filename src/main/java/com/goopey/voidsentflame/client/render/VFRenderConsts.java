package com.goopey.voidsentflame.client.render;

public class VFRenderConsts {
  /**
   * Used when an empty value is fine to get
  */
  public static class DefaultConsts {
    public static final int PACKED_LIGHT = 0;
    public static final int PACKED_OVERLAY = 0;
  }

  /**
   * Used when having a value unset is a major error.
   * Supposed to be a bright green (1, 247, 200) - #01f7c8.
   */
  public static class EmptyConsts {
    public static final int PACKED_LIGHT = 0;
    public static final int PACKED_OVERLAY = 0;
  }

  /**
   * Various render values the Rubicon uses.
   */
  public static class RubiconConsts {
    public static final int PACKED_LIGHT = 15728880;
    public static final int PACKED_OVERLAY = 655360;
  }
}
