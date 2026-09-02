package alfred;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link AlfredTheButler#getResponse(String)}.
 *
 * <p>That method is the only one in the class a unit test can see anything of.
 * The rest is the command loop, which reads from the console and writes to it,
 * and the text UI tests cover that from the outside.
 *
 * <p>The answer is a placeholder that echoes the line back, so what is worth
 * pinning is that the line comes back whole and unchanged after the prefix.
 * The window shows whatever this returns, so a line that came back trimmed or
 * rearranged would be visible to the user.
 */
public class AlfredTheButlerTest {
    @Test
    public void getResponse_typedLine_lineEchoedAfterPrefix() {
        assertEquals("Alfred heard: todo polish the silver",
                new AlfredTheButler().getResponse("todo polish the silver"));
    }

    @Test
    public void getResponse_emptyLine_prefixAlone() {
        assertEquals("Alfred heard: ", new AlfredTheButler().getResponse(""));
    }

    @Test
    public void getResponse_lineWithSurroundingSpaces_spacesKept() {
        assertEquals("Alfred heard:   list  ", new AlfredTheButler().getResponse("  list  "));
    }
}
